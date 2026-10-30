# Orders API acceptance fixture

POST JSON `{"id":"order-123"}` to `/orders`. Expected JSON: id, accepted status,
and USD currency loaded from resources. Missing id produces HTTP 400. A query
`testId` appears in the Logger. Native runtime acceptance packages and deploys
this same fixture. Choice/raise-error are executable core components; this
alpha's visual designer preserves them and makes the flow visually read-only.
