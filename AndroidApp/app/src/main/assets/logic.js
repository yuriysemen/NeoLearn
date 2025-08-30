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

const introductionSection = document.getElementById('introduction');
const goToLectureBtn = document.getElementById('go-to-lecture');
const goToTestBtn = document.getElementById('go-to-test');
const lectureSection = document.getElementById('lecture-section');
const testSection = document.getElementById('test-section');
const variants = document.querySelectorAll('.quiz-variant');
const nextPartitionBtn = document.getElementById('next-partition');
const prevPartitionBtn = document.getElementById('prev-partition');
const testResultsSection = document.getElementById('test-results-section');
const testResultsPointsLabel = document.getElementById('test-results-points');
const testResultsFinishBtn = document.getElementById('test-results-finish-lesson');
const testResultsGoAgainBtn = document.getElementById('test-results-go-again-lesson');

let visiblePartition = 1;
let partitionsCount = 1;

let chosenVariantId = 0;
let questionIndex = 1;

function goToPartition(number) {
    console.log('goToPartition(' + number + '). Current part number is ' + visiblePartition);

    for (var i = 1; i < 100000; i++) {
        if (document.querySelector(`#part` + i) == null) {
            break;
        }
        document.querySelector(`#part` + i).style.display = (i === number ? "block" : "none");
        partitionsCount = i;
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

if (nextPartitionBtn != null) {
    nextPartitionBtn.addEventListener('click', () => {
        console.log('goToNextPartition(). Current part number is ' + visiblePartition);
        goToPartition(visiblePartition + 1);
    });
}

if (prevPartitionBtn != null) {
    prevPartitionBtn.addEventListener('click', () => {
        console.log('goToPreviousPartition(). Current part number is ' + visiblePartition);
        goToPartition(visiblePartition - 1);
    });
}

function goToLectureFn() {
  console.log('goToLectureBtn click received...');

  introductionSection.style.display = 'none';
  lectureSection.style.display = 'block';
  testSection.style.display = 'none';

  console.log('Scrolling to page start');
  AndroidBridge.showPageFromStart();
  console.log('Scrolled to page start');

  goToPartition(1);
}

function makeTestInTestVariantVisible(questionNumber) {
  console.log('Variants count is ' + variants.length);
  for (var i = 0; i < variants.length; i++) {
    if (variants[i].id === chosenVariantId) {
      variants[i].style.display = 'block';

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

goToLectureBtn.addEventListener('click', goToLectureFn);

// При натисканні кнопки Закінчення лекції – показати випадковий варіант тесту
goToTestBtn.addEventListener('click', () => {
  console.log('goToTestBtn click received...');
  lectureSection.style.display = 'none';
  testSection.style.display = 'block';

  const variantIndex = Math.floor(Math.random() * variants.length);
  chosenVariantId = "variant" + (variantIndex + 1);
  TestingListener.testVariantWasChoose(chosenVariantId);
  console.log('Test variant chosen = ', chosenVariantId);

  questionIndex = 1;
  makeTestInTestVariantVisible(questionIndex);

  console.log('Scrolling to page start');
  AndroidBridge.showPageFromStart();
});

// Перевірка результатів
document.getElementById('check-test-question').addEventListener('click', () => {
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
  }
});

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

testResultsGoAgainBtn.addEventListener('click', () => {
    testResultsSection.style.display = 'none';
    goToLectureFn();
});
