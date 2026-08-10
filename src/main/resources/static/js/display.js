export function renderList(list, containerId, templateId, renderCallback) {
    const listContainer = document.getElementById(containerId);
    const template = document.getElementById(templateId);
    
    if (!listContainer || !template) return;

    listContainer.innerHTML = "";

    list.forEach(item => {
        const clone = template.content.cloneNode(true);
        renderCallback(clone, item);
        listContainer.appendChild(clone);
    });
}