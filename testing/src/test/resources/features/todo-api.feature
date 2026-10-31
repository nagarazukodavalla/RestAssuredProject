@api
Feature: Todo API
  Scenario: Retrieve a todo by ID
    Given the API request is configured
    When I send a GET request to "/todos/1"
    Then the response status should be 200
    And the response should contain todo id 1
    And the todo should have a title
