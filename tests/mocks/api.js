// Controls the local WireMock from Maestro flows. Each mocked route is a WireMock scenario
// and each variant is a scenario state. State "Started" means the route answers with its default
// mock from mappings/defaults.json.
const admin = `http://localhost:${MOCK_PORT}/__admin`
const headers = { 'Content-Type': 'application/json' }

function call(method, path, body) {
  const response = http.request(`${admin}${path}`, { method: method, headers: headers, body: body || '' })
  if (!response.ok) {
    throw new Error(`WireMock ${method} ${path} failed with ${response.status}: ${response.body}`)
  }
}

function set(route, variant) {
  call('PUT', `/scenarios/${route}/state`, JSON.stringify({ state: variant }))
}

output.mocks = {
  set: set,
  reset: () => call('POST', '/scenarios/reset'),
  // "stops:original,lines:added" -> set each route. Empty or missing string does nothing.
  setAll: (spec) => (spec || '').split(',').filter((s) => s.trim()).forEach((pair) => {
    const [route, variant] = pair.trim().split(':')
    set(route, variant)
  }),
}
