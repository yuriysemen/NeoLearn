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
const nextPartitionBtn = document.getElementById('next-partition')
const prevPartitionBtn = document.getElementById('prev-partition')

let variantIndex = 0;
let visiblePartition = 1;
let partitionsCount = 1;

function goToPartition(number) {
    console.log('goToPartition(' + number + '). Current part number is ' + visiblePartition);

    for (var i = 1; i < 100000; i++) {
        if (document.querySelector(`#part` + i) == null) {
            break;
        }
        document.querySelector(`#part` + i).style.display = (i === number ? "block" : "none");
        partitionsCount = i;
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

// При натисканні кнопки переходу до уроку – показати контекст уроку
goToLectureBtn.addEventListener('click', () => {
  console.log('goToLectureBtn click received...');

  introductionSection.style.display = 'none';
  lectureSection.style.display = 'block';
  testSection.style.display = 'none';
  variantIndex = Math.floor(Math.random() * variants.length);
  variants[variantIndex].style.display = 'block';

  console.log('Scrolling to page start');
  AndroidBridge.showPageFromStart();
  console.log('Scrolled to page start');

  goToPartition(1);
});

// При натисканні кнопки Закінчення лекції – показати випадковий варіант тесту
goToTestBtn.addEventListener('click', () => {
  console.log('goToTestBtn click received...');
  lectureSection.style.display = 'none';
  testSection.style.display = 'block';

  variantIndex = Math.floor(Math.random() * variants.length);
  console.log('Test variant choosed = ' + variantIndex);

  for (i = 0; i < variants.length; i++) {
    variants[i].style.display = 'none';
  }
  variants[variantIndex].style.display = 'block';

  console.log('Scrolling to page start');
  AndroidBridge.showPageFromStart();
  console.log('Scrolled to page start');
});

// Перевірка результатів
document.getElementById('check-test').addEventListener('click', () => {
  const activeVariant = document.querySelector(`.quiz-variant#variant${variantIndex + 1}`);
  const questions = activeVariant.querySelectorAll('[data-question-id]');

  const answers = {};

  questions.forEach(question => {
      const qId = question.dataset.questionId;
      const type = question.dataset.type; // Наприклад: "radio", "checkbox", "text"

      if (type === 'radio') {
          const selected = question.querySelector('input[type="radio"]:checked');
          answers[qId] = selected ? Array.of(selected.value) : [];

      } else if (type === 'checkbox') {
          const selected = question.querySelectorAll('input[type="checkbox"]:checked');
          answers[qId] = Array.from(selected).map(cb => cb.value);

      } else if (type === 'text') {
          const input = question.querySelector('input[type="text"], textarea');
          answers[qId] = input ? input.value.trim() : '';
      }
  });

  const result = {
      answers: answers,
      variantId: variantIndex + 1
  };

  if (window.AndroidBridge) {
      console.log('sendMaterialTestData("' + JSON.stringify(result) + '");');
      resultOfSendingData = AndroidBridge.sendMaterialTestData(JSON.stringify(result));
      console.log('sent MaterialTestData()' + resultOfSendingData);
  } else {
      console.log('Результат:', result);
      alert(JSON.stringify(result, null, 2));
  }
});
