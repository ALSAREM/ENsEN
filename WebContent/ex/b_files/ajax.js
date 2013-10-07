function processHTML(temp, target) {
	target.innerHTML = temp.innerHTML;
}
function displayHTML(url, storageID, viewedID) {
	var x = document.getElementById(viewedID);
	AALoadHTML(url, processHTML, storageID, x);
}