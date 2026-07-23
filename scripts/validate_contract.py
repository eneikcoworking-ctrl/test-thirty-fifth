#!/usr/bin/env python3
import json
import sys
import os
import yaml

def load_json_spec(path):
    if not os.path.exists(path):
        print(f"Error: JSON spec file '{path}' does not exist.")
        sys.exit(1)
    try:
        with open(path, 'r', encoding='utf-8') as f:
            return json.load(f)
    except json.JSONDecodeError as e:
        print(f"Error: '{path}' is not valid JSON: {e}")
        sys.exit(1)

def load_yaml_spec(path):
    if not os.path.exists(path):
        print(f"Error: YAML spec file '{path}' does not exist.")
        sys.exit(1)
    try:
        with open(path, 'r', encoding='utf-8') as f:
            return yaml.safe_load(f)
    except Exception as e:
        print(f"Error: '{path}' is not valid YAML: {e}")
        sys.exit(1)

def validate_openapi():
    base_dir = os.path.join(os.path.dirname(__file__), '..')
    json_path = os.path.normpath(os.path.join(base_dir, 'docs', 'openapi.json'))
    yaml_path = os.path.normpath(os.path.join(base_dir, 'docs', 'openapi.yaml'))
    api_yaml_path = os.path.normpath(os.path.join(base_dir, 'docs', 'api', 'openapi.yaml'))

    print("Loading and parsing contract files...")
    json_spec = load_json_spec(json_path)
    yaml_spec = load_yaml_spec(yaml_path)
    api_yaml_spec = load_yaml_spec(api_yaml_path)

    # 1. Verification of identity / semantic equivalence
    json_str_sorted = json.dumps(json_spec, sort_keys=True)
    yaml_str_sorted = json.dumps(yaml_spec, sort_keys=True)
    api_yaml_str_sorted = json.dumps(api_yaml_spec, sort_keys=True)

    if json_str_sorted != yaml_str_sorted:
        print("Error: Semantic drift detected! 'docs/openapi.json' and 'docs/openapi.yaml' are not identical in content.")
        sys.exit(1)
    print("Success: 'docs/openapi.json' and 'docs/openapi.yaml' are semantically identical.")

    if yaml_str_sorted != api_yaml_str_sorted:
        print("Error: Semantic drift detected! 'docs/api/openapi.yaml' does not match 'docs/openapi.yaml'.")
        sys.exit(1)
    print("Success: 'docs/api/openapi.yaml' and 'docs/openapi.yaml' are semantically identical.")

    # We will use the parsed json_spec for structural checks (as they are identical, checking one is checking all)
    data = json_spec

    # 2. Basic Structure Verification
    required_keys = ["openapi", "info", "paths", "components"]
    for key in required_keys:
        if key not in data:
            print(f"Error: Missing required OpenAPI top-level key '{key}'")
            sys.exit(1)

    # 3. Endpoints Verification
    paths = data["paths"]
    expected_endpoints = ["/api/auth/register", "/api/auth/login", "/api/bookmarks", "/api/bookmarks/{id}"]
    for endpoint in expected_endpoints:
        if endpoint not in paths:
            print(f"Error: Missing expected endpoint '{endpoint}'")
            sys.exit(1)

    # 4. Acceptance Criteria Check: Given the need to fetch and filter bookmarks,
    # query parameters for tags, title, and sorting are specified.
    bookmarks_get = paths.get("/api/bookmarks", {}).get("get", {})
    parameters = bookmarks_get.get("parameters", [])
    param_map = {p["name"]: p for p in parameters}

    for expected_param in ["tags", "title", "sort"]:
        if expected_param not in param_map:
            print(f"Error: Query parameter '{expected_param}' is missing in GET /api/bookmarks")
            sys.exit(1)

    # 5. Serialization Check for tags parameter (avoiding parsing ambiguities)
    tags_param = param_map["tags"]
    if tags_param.get("style") != "form" or tags_param.get("explode") is not False:
        print("Error: Query parameter 'tags' must explicitly specify style: form and explode: false.")
        sys.exit(1)
    print("Success: Query parameter 'tags' explicitly specifies style: form and explode: false.")

    # 6. Acceptance Criteria Check: Given bookmark creation,
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

    print("Success: All OpenAPI specifications are valid and meet all acceptance/style criteria perfectly!")

if __name__ == "__main__":
    validate_openapi()
