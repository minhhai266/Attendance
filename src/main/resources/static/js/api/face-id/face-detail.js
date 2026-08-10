import { renderList } from "/js/display.js";

async function fetchFaceDetails() {
    try {
        const response = await fetch("/api/face-id/details");
        if (!response.ok) {
            throw new Error("Failed to fetch face details");
        }
        const data = await response.json();
        return data;
    } catch (error) {
        console.error("Error fetching face details:", error);
        return [];
    }
}

function renderFaceDetails(details) {
    renderList(details, "faceDetailsContainer", "faceDetailTemplate", (clone, item) => {
        clone.querySelector(".face-id").textContent = item.faceId;
        clone.querySelector(".user-name").textContent = item.userName;
        clone.querySelector(".created-at").textContent = new Date(item.createdAt).toLocaleString();
    });
}

async function init() {
    const faceDetails = await fetchFaceDetails();
    renderFaceDetails(faceDetails);
}

document.addEventListener("DOMContentLoaded", init);