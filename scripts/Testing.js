

document.getElementById('generate-image').addEventListener('submit', function (event) {
  event.preventDefault(); // Prevent default form submission

  const question = document.getElementById("question").value;
  const xhr = new XMLHttpRequest();
  xhr.open("POST", "generate-image", true);
  xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");

  // Show loading text while the request is processed
  const generatedImageContainer = document.getElementById("generated-image-container");
  generatedImageContainer.innerHTML = "<p>Generating image...</p>";

  xhr.onreadystatechange = function () {
    if (xhr.readyState === 4) { // Check if the response is completeW
      if (xhr.status === 200) { // Check if the request was successful
        try {
          const response = JSON.parse(xhr.responseText);
          // Display the result in the page
          const imageElement = document.createElement("img");
          imageElement.id = "generated-image-element";
          imageElement.src = response.image_url;
          generatedImageContainer.innerHTML = "";
          generatedImageContainer.appendChild(imageElement);
        } catch (error) {
          console.error("Error parsing JSON:", error);
          generatedImageContainer.innerHTML = `<p style="color:red;">Failed to parse the response.</p>`;
        }
      } else {
        console.error("Error:", xhr.responseText);
        generatedImageContainer.innerHTML = `<p style="color:red;">Error: ${xhr.responseText}</p>`;
      }
    }
  };

  // Send the request
  const params = `question=${encodeURIComponent(question)}`;
  xhr.send(params);
});