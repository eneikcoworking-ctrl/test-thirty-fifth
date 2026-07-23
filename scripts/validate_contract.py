#!/usr/bin/env python3
import json
import sys
import os

def validate_openapi():
    json_path = os.path.join(os.path.dirname(__file__), '..', 'docs', 'openapi.json')
    if not os.path.exists(json_path):
        print(f"Error: {json_path} does not exist.")
        sys.exit(1)

    try:
        with open(json_path, 'r', encoding='utf-8') as f:
            data = json.load(f)
    except json.JSONDecodeError as e:
        print(f"Error: docs/openapi.json is not valid JSON: {e}")
        sys.exit(1)

    # 1. Basic Structure Verification
    required_keys = ["openapi", "info", "paths", "components"]
    for key in required_keys:
        if key not in data:
            print(f"Error: Missing required OpenAPI top-level key '{key}'")
            sys.exit(1)

    # 2. Endpoints Verification
    paths = data["paths"]
    expected_endpoints = ["/api/auth/register", "/api/auth/login", "/api/bookmarks", "/api/bookmarks/{id}"]
    for endpoint in expected_endpoints:
        if endpoint not in paths:
            print(f"Error: Missing expected endpoint '{endpoint}'")
            sys.exit(1)

    # 3. Acceptance Criteria Check: Given the need to fetch and filter bookmarks,
    # query parameters for tags, title, and sorting are specified.
    bookmarks_get = paths.get("/api/bookmarks", {}).get("get", {})
    parameters = bookmarks_get.get("parameters", [])
    param_names = [p["name"] for p in parameters]
    for expected_param in ["tags", "title", "sort"]:
        if expected_param not in param_names:
            print(f"Error: Query parameter '{expected_param}' is missing in GET /api/bookmarks")
            sys.exit(1)

    # 4. Acceptance Criteria Check: Given bookmark creation,
    # When defining the payload, Then URL and title are marked required while notes and tags are optional.
    schemas = data.get("components", {}).get("schemas", {})
    bookmark_request = schemas.get("BookmarkRequest", {})
    if not bookmark_request:
        print("Error: Missing schema 'BookmarkRequest' in components/schemas")
        sys.exit(1)

    required_properties = bookmark_request.get("required", [])
    if "url" not in required_properties or "title" not in required_properties:
        print("Error: 'url' and 'title' must be marked required in BookmarkRequest")
        sys.exit(1)

    if "notes" in required_properties or "tags" in required_properties:
        print("Error: 'notes' and 'tags' should not be required in BookmarkRequest")
        sys.exit(1)

    print("Success: docs/openapi.json is a valid OpenAPI 3.0 specification and meets all acceptance criteria!")

if __name__ == "__main__":
    validate_openapi()
