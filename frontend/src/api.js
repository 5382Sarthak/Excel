
const api = async (endpoint, options = {}) => {
  const response = await fetch(endpoint, {
    ...options,
    credentials: "include",
  });

  return response;
};

export default api;