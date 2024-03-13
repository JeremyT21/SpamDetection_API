//onload function should retrieve the data needed to populate the UI
async function addData() {
  URL = "http://localhost:8080/spamDetector-1.0/api/spam";

  // Code adds the testing values to the rows of the Dashboard table
  let response = await fetch(URL);
  let responseValues = await response.json();
  let tableBody = document.querySelector('#chart tbody')

  // Loop goes through and adds every row to the table
  for (let i=0; i<responseValues.length; i++)
  {
    let newRow = tableBody.insertRow();
    newRow.innerHTML = "<tr><td>"+responseValues[i].filename+"</td>"+"<td>"+(responseValues[i].spamProbability*100).toFixed(2)+"</td>"+"<td>"+responseValues[i].actualClass+"</td></tr>";
  }

  // Code updates the accuracy and precision values on the Dashboard
  let precisionElement = document.getElementById('precisionValue');
  let precisionValue = await fetch(URL + '/precision');
  let precisionPercent = await (precisionValue.text());
  precisionPercent *= 100; //*100 for value as a %
  precisionElement.innerText = precisionPercent.toFixed(2);

  let accuracyElement = document.getElementById('accuracyValue');
  let accuracyValue = await fetch(URL + '/accuracy');
  let accuracyPercent = await (accuracyValue.text());
  accuracyPercent *= 100; //*100 for value as a %
  accuracyElement.innerText = accuracyPercent.toFixed(2);
}
