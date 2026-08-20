async function fetchAPI(url, method, body = null) {
  const options = {
    method: method,
    headers: {
      "Content-Type": "application/json"
    }
  };
  if (body) {
    options.body = JSON.stringify(body);
  }

  const response = await fetch(url, options);

  if (!response.ok) {
    const err = await response.text();
    throw new Error(err);
  }
  
  return response.json();
}

export const get = (url) => fetchApi(url, "GET");
export const post = (url, body) => fetchApi(url, "POST", body);
export const put = (url, body) => fetchApi(url, "PUT", body);
export const patch = (url, body) => fetchApi(url, "PATCH", body);