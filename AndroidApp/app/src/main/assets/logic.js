if (window.AndroidBridge) {
  // Hide button next
  console.log('Hiding original next button');
  AndroidBridge.showNextBtn(false);
  console.log('Hided original next button');

  // Going to start of the page
  console.log('Scrolling to page start');
  AndroidBridge.showPageFromStart();
  console.log('Scrolled to page start');
} else {
  alert(`Trying to hide next button failed.`);
}

const validationMessage = document.getElementById("validation-message");
const introductionSection = document.getElementById('introduction');
const goToLectureBtn = document.getElementById('go-to-lecture');
const goToTestBtn = document.getElementById('go-to-test');
const lectureSection = document.getElementById('lecture-section');
const testSection = document.getElementById('test-section');
const variants = document.querySelectorAll('.quiz-variant');
const nextPartitionBtn = document.getElementById('next-partition');
const prevPartitionBtn = document.getElementById('prev-partition');
const checkTestQuestionBtn = document.getElementById('next-test-question');
const testResultsSection = document.getElementById('test-results-section');
const testResultsPointsLabel = document.getElementById('test-results-points');
const testResultsFinishBtn = document.getElementById('test-results-finish-lesson');
const testResultsGoAgainBtn = document.getElementById('test-results-go-again-lesson');

let visiblePartition = 1;
let partitionsCount = 1;

let chosenVariantId = -1;
let questionIndex = 1;

let historyStack = [];
historyStack.push("introduction");

function showValidationIssue(message) {
    if (validationMessage != null) {
        validationMessage.style.display = "block";
        validationMessage.innerText = message;
    }
}

function hideValidationIssue(message) {
    if (validationMessage != null) {
        validationMessage.style.display = "none";
    }
}

function hideEverything() {
    introductionSection.style.display = "none";
    lectureSection.style.display = "none";
    testSection.style.display = 'none';
    testResultsSection.style.display = 'none';
}

function goToPagePart(backTo) {
    console.log("goToPagePart to " + backTo);
    hideEverything();
    if (backTo == "introduction") {
        introductionSection.style.display = "block";
    }

    if (backTo.startsWith("go to partition")) {
        const match = backTo.match(/\d+/);

        if (match) {
            const number = parseInt(match[0], 10);
            console.log("Go to partition from history " + number);
            goToLearnPartition(number)
        }
    }

    if (backTo.startsWith("go to test part ")) {
        const match = backTo.match(/\d+/);

        if (match) {
            const number = parseInt(match[0], 10);
            console.log("Go to test (from history) number " + number);
            goToTestFn(false, number)
        }
    }

    AndroidBridge.showPageFromStart();
}

function handleBack() {
    console.log("HistoryStack length is: " + historyStack.length);
    if (historyStack.length > 1) {
        historyStack.pop();
        var fromStack = historyStack.at(-1)
        console.log("What is the latest in stack: " + fromStack);
        goToPagePart(fromStack);
        return true;
    } else {
        return false;
    }
}

function goToLearnPartition(number) {
    console.log('goToLearnPartition(' + number + '). Current part number is ' + visiblePartition);

    for (var i = 1; i < 100000; i++) {
        if (document.querySelector(`#part` + i) == null) {
            break;
        }
        document.querySelector(`#part` + i).style.display = (i === number ? "block" : "none");
        partitionsCount = i;
        lectureSection.style.display = "block";
        AndroidBridge.showPageFromStart();
    }

    console.log('Partitions count is ' + partitionsCount);

    if (prevPartitionBtn != null) {
        prevPartitionBtn.style.display = (number == 1 ? 'none' : 'block');
        console.log('Back button visibility is ' + prevPartitionBtn.style.display);
    }
    if (nextPartitionBtn != null) {
        nextPartitionBtn.style.display = (number == partitionsCount ? 'none' : 'block');
        console.log('Next button visibility is ' + nextPartitionBtn.style.display);
    }
    visiblePartition = number;
    goToTestBtn.style.display = (number != partitionsCount ? 'none' : 'block');
};

function makeTestInTestVariantVisible(questionNumber, fillHistory = true) {
  console.log('Variants count is ' + variants.length);
  for (var i = 0; i < variants.length; i++) {
    if (variants[i].id === chosenVariantId) {
      variants[i].style.display = 'block';
      if (fillHistory) historyStack.push("go to test part " + questionNumber);

      var questions = variants[i].querySelectorAll('.question');
      console.log('Questions count is ' + questions.length);

      for (var i = 0; i < questions.length; i++) {
        questions[i].style.display = (i === questionNumber - 1 ? "block" : "none");
      }
    }
    else {
      variants[i].style.display = 'none';
    }
  }
}

function goToLectureFn() {
  testResultsSection.style.display = 'none';

  introductionSection.style.display = 'none';
  lectureSection.style.display = 'block';
  testSection.style.display = 'none';

  console.log('goToLectureBtn click received...');
  console.log('Scrolling to page start');
  AndroidBridge.showPageFromStart();
  console.log('Scrolled to page start');

  historyStack.push("go to partition " + 1);
  goToLearnPartition(1);
}

function cleanAllChooses(variantId) {
    console.log('CleanAllChooses...');
    for (var i = 0; i < variants.length; i++) {
        variants[i].querySelectorAll('input[type=radio], input[type=checkbox]')
            .forEach(input => {
                console.log('Renew choosed state: ' + input.name);
                input.checked = false;
                input.disabled = false;
            });
    }
}

function goToTestFn(fillHistory = true, testNumber = 1) {
    console.log('goToTestBtn click received...');

    hideEverything();
    testSection.style.display = 'block';

    if (chosenVariantId < 0) {
        chosenVariantId = "variant" + (Math.floor(Math.random() * variants.length) + 1);
        cleanAllChooses(chosenVariantId);
        TestingListener.testVariantWasChoose(chosenVariantId);
        console.log('Test variant chosen = ', chosenVariantId);
    }

    makeTestInTestVariantVisible(testNumber, fillHistory);
    questionIndex = testNumber;

    console.log('Scrolling to page start');
    AndroidBridge.showPageFromStart();
}

function goToTestResults() {
  console.log('goToTestResultsBtn click received...');
  testSection.style.display = 'none';
  testResultsSection.style.display = 'block';

  const testingResults = JSON.parse(TestingListener.getTestResult());
  console.log('test results: ' + testingResults);

  testResultsPointsLabel.innerText = testingResults.collectedPoints + "/" + testingResults.totalPoints;
  if (testingResults.collectedPoints >= testingResults.totalPoints * 0.9) {
    testResultsFinishBtn.style.display = 'block';
    testResultsGoAgainBtn.style.display = 'none';
  }
  else {
    testResultsFinishBtn.style.display = 'none';
    testResultsGoAgainBtn.style.display = 'block';
  }
}

function checkTestAnswersFn() {
  const activeVariant = document.querySelector(`.quiz-variant#${chosenVariantId}`);
  const questions = activeVariant.querySelectorAll('[data-question-id]');

  question = questions[questionIndex - 1];

  const qId = question.dataset.questionId;
  const type = question.dataset.type;

  if (type === 'radio') {
      const selected = question.querySelector('input[type="radio"]:checked');
      answeredValue = selected ? Array.of(selected.value) : [];

  } else if (type === 'checkbox') {
      const selected = question.querySelectorAll('input[type="checkbox"]:checked');
      answeredValue = Array.from(selected).map(cb => cb.value);

  }

  if (!answeredValue.length) {
    showValidationIssue("Please choose answer");
    return;
  }
  hideValidationIssue("Please choose answer");

  question.querySelectorAll('input[type="radio"], input[type=checkbox]').forEach(el => el.disabled = true);

  const result = {
      variantId: chosenVariantId,
      questionId: qId,
      answer: answeredValue
  };

  if (window.AndroidBridge) {
      console.log('sendMaterialTestData("' + JSON.stringify(result) + '");');
      resultOfSendingData = TestingListener.sendMaterialTestData(JSON.stringify(result));
      console.log('sent MaterialTestData()' + resultOfSendingData);
  } else {
      console.log('Результат: ', result);
      alert(JSON.stringify(result, null, 2));
  }

  if (questionIndex < questions.length) {
    questionIndex += 1;
    makeTestInTestVariantVisible(questionIndex);
  } else {
    goToTestResults()
    makeTestInTestVariantVisible(-1);
  }
}

if (nextPartitionBtn != null) {
    nextPartitionBtn.addEventListener('click', () => {
        console.log('goToNextPartition(). Current part number is ' + visiblePartition);
        historyStack.push("go to partition " + (visiblePartition + 1));
        goToLearnPartition(visiblePartition + 1);
    });
}

if (prevPartitionBtn != null) {
    prevPartitionBtn.addEventListener('click', () => {
        console.log('goToPreviousPartition(). Current part number is ' + visiblePartition);
        historyStack.push("go to partition " + (visiblePartition + 1));
        goToLearnPartition(visiblePartition - 1);
    });
}

testResultsGoAgainBtn.addEventListener('click', () => {
    console.log("Go to start of lesson.")
    chosenVariantId = -1;
    goToLectureFn();
})

goToLectureBtn.addEventListener('click', goToLectureFn);
goToTestBtn.addEventListener('click', goToTestFn);

checkTestQuestionBtn.addEventListener('click', checkTestAnswersFn)
