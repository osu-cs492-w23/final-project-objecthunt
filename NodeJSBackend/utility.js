function shuffleArray(array) {
    let m = array.length, t, i;

    // While there remain elements to shuffle…
    while (m) {

        // Pick a remaining element…
        i = Math.floor(Math.random() * m--);

        // And swap it with the current element.
        t = array[m];
        array[m] = array[i];
        array[i] = t;
    }

    return array;
}

function distance(item1, item2) {
    const R = 6371; // Radius of the earth in km
    const dLat = deg2rad(item2["latitude"] - item1["latitude"]); // deg2rad below
    const dLon = deg2rad(item2["longtitude"] - item1["longtitude"]);
    const a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(deg2rad(item1["latitude"])) *
        Math.cos(deg2rad(item2["latitude"])) *
        Math.sin(dLon / 2) *
        Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
     // Distance in km
    return R * c;
}

function deg2rad(deg) {
    return deg * (Math.PI / 180);
}

module.exports = {
    shuffleArray: shuffleArray,
    distance: distance
}