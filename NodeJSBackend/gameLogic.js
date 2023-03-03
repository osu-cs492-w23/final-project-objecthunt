
async function quickstart(buffer) {
    // Imports the Google Cloud client library
    const vision = require('@google-cloud/vision');

    // Creates a client
    const client = new vision.ImageAnnotatorClient();

    const request = {
        image: {
            content: buffer.toString("base64"),
        },
    };

    // Performs label detection on the image file
    const [result] = await client.labelDetection(request);
    const labels = result.labelAnnotations;
    labels.forEach(label => console.log(label.description))
    return labels.map(label => label.description.toLowerCase());
}

module.exports = {
    "quickstart": quickstart
}