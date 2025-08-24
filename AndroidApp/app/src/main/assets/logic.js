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

let variantIndex = 0;

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
