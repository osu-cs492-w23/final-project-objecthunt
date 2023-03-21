function fixBase64Encoding(base64Image) {
    // Ensure padding is added if missing
    const paddedBase64Image = base64Image.padEnd(base64Image.length + (4 - (base64Image.length % 4)) % 4, '=');
    
    // Replace URL-safe characters with their base64 counterparts
    const fixedBase64Image = paddedBase64Image.replace(/-/g, '+').replace(/_/g, '/');
    
    return fixedBase64Image;
  }
  

  async function checkImage(base64Image) {
    // Imports the Google Cloud client library
    const vision = require('@google-cloud/vision');
    //for testing on my machine (windows)
    // process.env.GOOGLE_APPLICATION_CREDENTIALS = "/mnt/c/Users/Adil/Desktop/googleapi/googlevision.json"
    
    // Creates a client
    const client = new vision.ImageAnnotatorClient();
  
    // Fix the base64 encoding if necessary
    const fixedBase64Image = fixBase64Encoding(base64Image);
  
    const request = {
      image: {
        content: fixedBase64Image,
      },
    };
  
    // Performs label detection on the image file
    const [result] = await client.labelDetection(request);
    const labels = result.labelAnnotations;
    labels.forEach(label => console.log(label.description));
    return labels.map(label => label.description.toLowerCase());
  }
  


  module.exports = {
    "checkImage": checkImage
}