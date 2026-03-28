## ADDED Requirements
### Requirement: Push Subscription Registration
The system SHALL allow the frontend to register a browser Web Push subscription for later notification delivery.

#### Scenario: Register a new subscription
- **WHEN** the client sends a valid subscription payload with `endpoint`, `p256dh`, and `auth`
- **THEN** the system stores the subscription details
- **AND** returns a successful response

#### Scenario: Re-register an existing endpoint
- **WHEN** the client sends a subscription payload for an endpoint that already exists
- **THEN** the system updates the stored subscription metadata
- **AND** does not create a duplicate record

### Requirement: Push Subscription Removal
The system SHALL allow the frontend to remove a stored browser subscription.

#### Scenario: Remove an existing subscription
- **WHEN** the client sends a valid endpoint for an existing subscription
- **THEN** the system deletes the stored subscription
- **AND** returns a successful response

#### Scenario: Remove a non-existing subscription
- **WHEN** the client sends an endpoint that is not stored
- **THEN** the system returns a successful response without error

### Requirement: Web Push Notification Delivery
The system SHALL send Web Push notifications to stored subscriptions using VAPID configuration.

#### Scenario: Send a test notification
- **WHEN** an authorized client requests a push test with a valid payload
- **THEN** the system sends a Web Push notification to stored subscriptions
- **AND** returns the delivery result summary

#### Scenario: Handle an invalid subscription during send
- **WHEN** a stored subscription is no longer accepted by the push provider
- **THEN** the system excludes that subscription from future sends
- **AND** continues processing the remaining subscriptions

### Requirement: Push Configuration Validation
The system SHALL fail fast when required VAPID configuration is missing.

#### Scenario: Missing VAPID configuration on send
- **WHEN** a push send is requested without required VAPID keys configured
- **THEN** the system rejects the request with a clear server-side error
