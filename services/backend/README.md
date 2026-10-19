# Green MindMap Backend API - Postman Guide

## Authentication
Tất cả các API đều yêu cầu JWT token trong header:
```
Authorization: Bearer <your_jwt_token>
```

## API Endpoints

### [Users] POST /api/users/register
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "password123",
  "fullName": "John Doe",
  "phoneNumber": "0901234567",
  "dateOfBirth": "1990-01-01T00:00:00.000Z"
}
```

### [Users] POST /api/users/login
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

### [Users] GET /api/users/profile

### [Users] PUT /api/users/update
```json
{
  "fullName": "John Doe Updated",
  "phoneNumber": "0987654321"
}
```

### [Users] DELETE /api/users/delete

### [Templates] POST /api/templates/create
```json
{
  "id": "T_CUSTOM_01",
  "name": "Custom Template",
  "description": "Template description",
  "intent": "frequency",
  "prompt": "How often do you {keywords}?",
  "used_placeholders": ["keywords"],
  "question_type": "frequency",
  "filled_prompt": "How often do you eat healthy food?",
  "answer": {
    "type": "scale",
    "scale": [1, 2, 3, 4],
    "labels": ["Never", "Sometimes", "Often", "Always"]
  }
}
```

### [Templates] POST /api/templates/createTemplates
```json
{
  "templates": [
    {
      "id": "T_EXAMPLE_01",
      "name": "Example frequency template",
      "description": "Placeholder example (real templates are private).",
      "intent": "frequency",
      "placeholders": {
        "required": ["keywords"],
        "optional": ["age"],
        "used_placeholders": ["keywords"]
      },
      "prompt": "How often do you {keywords}?",
      "question_type": "frequency",
      "answer": {
        "type": "scale",
        "scale": [1, 2, 3, 4],
        "labels": ["Never", "Sometimes", "Often", "Always"]
      },
      "filled_prompt": "How often do you recycle plastic bottles?"
    },
    {
      "id": "T_EXAMPLE_02",
      "name": "Example yes/no template",
      "description": "Placeholder example (real templates are private).",
      "intent": "yesno",
      "placeholders": {
        "required": ["keywords"],
        "optional": [],
        "used_placeholders": ["keywords"]
      },
      "prompt": "Do you {keywords}?",
      "question_type": "yesno",
      "answer": {
        "type": "binary",
        "options": ["Yes", "No"]
      },
      "filled_prompt": "Do you sort your waste?"
    }
  ]
}
```

### [Templates] GET /api/templates/getAll

### [Templates] GET /api/templates/getById/:id

### [Templates] PUT /api/templates/update
```json
{
  "id": "T_CUSTOM_01",
  "name": "Updated Template Name",
  "description": "Updated description",
  "intent": "likert5",
  "prompt": "Updated prompt text",
  "answer": {
    "type": "scale",
    "scale": [1, 2, 3, 4, 5],
    "labels": ["Strongly Disagree", "Disagree", "Neutral", "Agree", "Strongly Agree"]
  }
}
```

### [Templates] DELETE /api/templates/delete/:id

### [Behaviors] POST /api/behaviors/create
```json
{
  "name": "Eating Behavior",
  "type": "daily",
  "keywords": ["eat", "food", "meal"],
  "description": "Daily eating habits"
}
```

### [Behaviors] GET /api/behaviors/getAll

### [Behaviors] GET /api/behaviors/getById/:id

### [Behaviors] PUT /api/behaviors/update
```json
{
  "id": "behavior_id",
  "name": "Updated Behavior",
  "type": "weekly",
  "keywords": ["updated", "keywords"],
  "description": "Updated description"
}
```

### [Behaviors] DELETE /api/behaviors/delete/:id

### [Questions] POST /api/questions/create
```json
{
  "question": "How often do you exercise?",
  "templateId": "template_uuid",
  "behaviorInput": "exercise",
  "behaviorNormalized": "physical_activity",
  "normalizeScore": 4.5
}
```

### [Questions] POST /api/questions/createQuestions
```json
{
  "questions": [
    {
      "id": "T_EXAMPLE_01",
      "name": "Example frequency template",
      "intent": "frequency",
      "question_type": "frequency",
      "filled_prompt": "How often do you recycle plastic bottles?",
      "answer": {
        "type": "scale",
        "scale": [1, 2, 3, 4],
        "labels": ["Never", "Sometimes", "Often", "Always"]
      }
    },
    {
      "id": "T_EXAMPLE_02",
      "name": "Example yes/no template",
      "intent": "yesno",
      "question_type": "yesno",
      "filled_prompt": "Do you sort your waste?",
      "answer": {
        "type": "binary",
        "options": ["Yes", "No"]
      }
    }
  ]
}
```

### [Questions] GET /api/questions/getAll

### [Questions] GET /api/questions/getById/:id

### [Questions] PUT /api/questions/update
```json
{
  "id": "question_uuid",
  "question": "Updated question text",
  "behaviorInput": "updated input",
  "normalizeScore": 3.8
}
```

### [Questions] DELETE /api/questions/delete/:id

### [UserAnswers] POST /api/userAnswers/create
```json
{
  "question_id": "question_uuid",
  "answer": "Often",
  "timestamp": "2025-01-10T10:00:00.000Z"
}
```

### [UserAnswers] GET /api/userAnswers/getAll

### [UserAnswers] GET /api/userAnswers/getById/:userId/:questionId

### [UserAnswers] PUT /api/userAnswers/update
```json
{
  "user_id": "user_uuid",
  "question_id": "question_uuid",
  "answer": "Updated answer",
  "timestamp": "2025-01-10T11:00:00.000Z"
}
```

### [UserAnswers] DELETE /api/userAnswers/delete/:userId/:questionId

### [ThreadHalls] POST /api/threadHalls/create
```json
{
  "name": "Personality Thread",
  "description": "Thread for personality assessment",
  "traitsId": "trait_uuid"
}
```

### [ThreadHalls] GET /api/threadHalls/getAll

### [ThreadHalls] GET /api/threadHalls/getById/:id

### [ThreadHalls] PUT /api/threadHalls/update
```json
{
  "id": "threadhall_uuid",
  "name": "Updated Thread Name",
  "description": "Updated description"
}
```

### [ThreadHalls] DELETE /api/threadHalls/delete/:id

### [Traits] POST /api/traits/create
```json
{
  "name": "Openness",
  "description": "Openness to experience",
  "label": "O"
}
```

### [Traits] GET /api/traits/getAll

### [Traits] GET /api/traits/getById/:id

### [Traits] PUT /api/traits/update
```json
{
  "id": "trait_uuid",
  "name": "Updated Trait",
  "description": "Updated description",
  "label": "UT"
}
```

### [Traits] DELETE /api/traits/delete/:id

### [BigFive] POST /api/bigFive/create
```json
{
  "openness": 4.2,
  "conscientiousness": 3.8,
  "extraversion": 4.5,
  "agreeableness": 4.0,
  "neuroticism": 2.3
}
```

### [BigFive] GET /api/bigFive/getByUserId/:userId

### [BigFive] PUT /api/bigFive/update
```json
{
  "openness": 4.5,
  "conscientiousness": 4.0,
  "extraversion": 4.2,
  "agreeableness": 4.3,
  "neuroticism": 2.1
}
```

### [BigFive] DELETE /api/bigFive/delete/:userId

### [Locations] POST /api/locations/create
```json
{
  "latitude": 15.5753,
  "longitude": 108.4834,
  "address": "Quảng Nam, Vietnam"
}
```

### [Locations] GET /api/locations/getAll

### [Locations] GET /api/locations/getById/:id

### [Locations] PUT /api/locations/update
```json
{
  "id": "location_uuid",
  "latitude": 15.6000,
  "longitude": 108.5000,
  "address": "Updated address"
}
```

### [Locations] DELETE /api/locations/delete/:id

### [FoodItems] POST /api/foodItems/create
```json
{
  "name": "Mỳ Quảng",
  "barcode": "123456789",
  "caloriesId": "calories_uuid"
}
```

### [FoodItems] GET /api/foodItems/getAll

### [FoodItems] GET /api/foodItems/getById/:id

### [FoodItems] PUT /api/foodItems/update
```json
{
  "id": "fooditem_uuid",
  "name": "Updated Food Name",
  "barcode": "987654321"
}
```

### [FoodItems] DELETE /api/foodItems/delete/:id

### [Calories] POST /api/calories/create
```json
{
  "energy_kcal": 350.5,
  "protein_g": 12.3,
  "fat_g": 8.7,
  "carbs_g": 45.2
}
```

### [Calories] GET /api/calories/getAll

### [Calories] GET /api/calories/getById/:id

### [Calories] PUT /api/calories/update
```json
{
  "id": "calories_uuid",
  "energy_kcal": 400.0,
  "protein_g": 15.0,
  "fat_g": 10.0,
  "carbs_g": 50.0
}
```

### [Calories] DELETE /api/calories/delete/:id

### [Scans] POST /api/scans/create
```json
{
  "scan_time": "2025-01-10T10:00:00.000Z",
  "foodItemsId": "fooditem_uuid"
}
```

### [Scans] GET /api/scans/getAll

### [Scans] GET /api/scans/getById/:id

### [Scans] PUT /api/scans/update
```json
{
  "id": "scan_uuid",
  "scan_time": "2025-01-10T11:00:00.000Z",
  "foodItemsId": "updated_fooditem_uuid"
}
```

### [Scans] DELETE /api/scans/delete/:id

### [Invoices] POST /api/invoices/create
```json
{
  "issued_at": "10:00:00+07:00",
  "scansId": "scan_uuid"
}
```

### [Invoices] GET /api/invoices/getAll

### [Invoices] GET /api/invoices/getById/:id

### [Invoices] PUT /api/invoices/update
```json
{
  "id": "invoice_uuid",
  "issued_at": "11:00:00+07:00",
  "scansId": "updated_scan_uuid"
}
```

### [Invoices] DELETE /api/invoices/delete/:id

### [Tokens] POST /api/tokens/create
```json
{
  "token": "jwt_token_here",
  "deviceID": "device123",
  "expiredAt": "2025-02-10T10:00:00.000Z"
}
```

### [Tokens] GET /api/tokens/getAll

### [Tokens] GET /api/tokens/getById/:id

### [Tokens] PUT /api/tokens/update
```json
{
  "id": "token_uuid",
  "token": "updated_token",
  "deviceID": "updated_device",
  "expiredAt": "2025-03-10T10:00:00.000Z"
}
```

### [Tokens] DELETE /api/tokens/delete/:id

### [Health] GET /api/health/check

## Notes
- Tất cả timestamp phải theo format ISO 8601
- UUID sẽ được tự động generate nếu không cung cấp
- CORS đã được cấu hình cho phép tất cả origins (*)
- Tất cả API (trừ register/login) đều cần JWT token
