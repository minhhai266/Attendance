async function post(url, body) {
  const response = await fetch(url, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  });
  if (!response.ok) {
    const err = await response.text();
    throw new Error(err);
  }
  return await response.json();
}

async function get(url) {
    const response = await fetch(
      url, {
        method: "GET",
        headers: {
          "Content-Type": "application/json"
        }
      }
    );
    if(!response.ok){
      const err = await response.text();
      throw new Error(err);
    }
    return response.json();
}