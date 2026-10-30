%dw 2.0
output application/json
---
{message:p("app.message"),environment:p("zernova.env"),testId:attributes.queryParams.testId default "manual"}
