#!/bin/bash
# Register and get token
TOKEN=$(curl -s -X POST -H "Content-Type: application/json" -d '{"email":"testprovider999@example.com","password":"password","firstName":"Test","lastName":"Provider","role":"PROVIDER"}' http://localhost:8080/api/auth/register | grep -o '"token":"[^"]*' | grep -o '[^"]*$')

echo "Token: $TOKEN"

# Try getting profile
curl -v -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/providers/me

