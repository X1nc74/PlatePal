# PlatePal — Use Case Diagram

## Actor associations

| # | Use case | Regular User | Administrator |
|---|---|:---:|:---:|
| UC01 | Register Account | ● | |
| UC02 | Log In | ● | ● |
| UC03 | Search Restaurants | ● | |
| UC04 | View Restaurant Details | ● | |
| UC05 | Add Restaurant to Want to Try | ● | |
| UC06 | Mark Restaurant as Visited | ● | |
| UC07 | Submit Rating | ● | |
| UC08 | Write Review | ● | |
| UC09 | View Personal Restaurant Ranking | ● | |
| UC10 | Follow or Add Another User | ● | |
| UC11 | View Friend Profile | ● | |
| UC12 | View Friend Activity | ● | |
| UC13 | Add Restaurant | | ● |
| UC14 | Update Restaurant Information | | ● |
| UC15 | Remove Restaurant | | ● |
| UC16 | Remove Review or Rating  | | ● |

---

## Use case descriptions

### UC01 — Register Account

| | |
|---|---|
| **UC Reference Name/Number** | UC01 — Register Account |
| **Overview** | The user supplies account details and credentials, and the system creates a new PlatePal account so the user can build and store their personal restaurant lists. |
| **Related use cases** | None. |
| **Actors** | Regular User |

### UC02 — Log In

| | |
|---|---|
| **UC Reference Name/Number** | UC02 — Log In |
| **Overview** | The user enters their credentials and the system authenticates them, granting access to the features permitted by their role. A single shared use case serves both roles. |
| **Related use cases** | None. Authentication is a precondition of the other use cases, not an included behaviour. |
| **Actors** | Regular User, Administrator |

### UC03 — Search Restaurants

| | |
|---|---|
| **UC Reference Name/Number** | UC03 — Search Restaurants |
| **Overview** | The user enters search terms and the system returns the matching restaurants from the catalog as a browsable list of results. |
| **Related use cases** | None. |
| **Actors** | Regular User |

### UC04 — View Restaurant Details

| | |
|---|---|
| **UC Reference Name/Number** | UC04 — View Restaurant Details |
| **Overview** | The system displays the full record for a selected restaurant, including its details, ratings, and reviews. Reachable from search results, the user's own lists, or a friend's profile. |
| **Related use cases** | None. |
| **Actors** | Regular User |

### UC05 — Add Restaurant to Want to Try

| | |
|---|---|
| **UC Reference Name/Number** | UC05 — Add Restaurant to Want to Try |
| **Overview** | The user saves a restaurant to their Want to Try list and the system records it against their account for later reference. |
| **Related use cases** | None. |
| **Actors** | Regular User |

### UC06 — Mark Restaurant as Visited

| | |
|---|---|
| **UC Reference Name/Number** | UC06 — Mark Restaurant as Visited |
| **Overview** | The system records that the user has visited a restaurant, moving it onto their Visited list. The user may perform this on its own without rating or reviewing the restaurant. |
| **Related use cases** | Included by UC07 Submit Rating and UC08 Write Review. Remains independently available to the user. |
| **Actors** | Regular User |

### UC07 — Submit Rating

| | |
|---|---|
| **UC Reference Name/Number** | UC07 — Submit Rating |
| **Overview** | The user gives a restaurant a score and the system stores it against their account, contributing to their personal ranking. Submitting a rating always marks the restaurant as visited. |
| **Related use cases** | `<<include>>` UC06 Mark Restaurant as Visited. |
| **Actors** | Regular User |

### UC08 — Write Review

| | |
|---|---|
| **UC Reference Name/Number** | UC08 — Write Review |
| **Overview** | The user writes a written review of a restaurant and the system stores and publishes it. Posting a review always marks the restaurant as visited. |
| **Related use cases** | `<<include>>` UC06 Mark Restaurant as Visited. |
| **Actors** | Regular User |

### UC09 — View Personal Restaurant Ranking

| | |
|---|---|
| **UC Reference Name/Number** | UC09 — View Personal Restaurant Ranking |
| **Overview** | The system presents the restaurants the user has rated, ordered by their own scores, giving them a personal ranking of the places they have been. |
| **Related use cases** | None. |
| **Actors** | Regular User |

### UC10 — Follow or Add Another User

| | |
|---|---|
| **UC Reference Name/Number** | UC10 — Follow or Add Another User |
| **Overview** | The user locates another PlatePal user and follows them, and the system records the connection so that user's profile and activity become visible. |
| **Related use cases** | None. |
| **Actors** | Regular User |

### UC11 — View Friend Profile

| | |
|---|---|
| **UC Reference Name/Number** | UC11 — View Friend Profile |
| **Overview** | The system displays a followed user's profile, including their restaurant lists and ratings. |
| **Related use cases** | None. |
| **Actors** | Regular User |

### UC12 — View Friend Activity

| | |
|---|---|
| **UC Reference Name/Number** | UC12 — View Friend Activity |
| **Overview** | The system displays recent activity from the users the user follows, such as visits, ratings, and reviews they have posted. |
| **Related use cases** | None. |
| **Actors** | Regular User |

### UC13 — Add Restaurant

| | |
|---|---|
| **UC Reference Name/Number** | UC13 — Add Restaurant |
| **Overview** | The administrator enters the details of a new restaurant and the system adds it to the catalog, making it available to all users. |
| **Related use cases** | None. |
| **Actors** | Administrator |

### UC14 — Update Restaurant Information

| | |
|---|---|
| **UC Reference Name/Number** | UC14 — Update Restaurant Information |
| **Overview** | The administrator edits the stored details of an existing restaurant and the system saves the corrected record to the catalog. |
| **Related use cases** | None. |
| **Actors** | Administrator |

### UC15 — Remove Restaurant

| | |
|---|---|
| **UC Reference Name/Number** | UC15 — Remove Restaurant |
| **Overview** | The administrator removes a restaurant and the system deletes it from the catalog so it no longer appears in searches or listings. |
| **Related use cases** | None. |
| **Actors** | Administrator |

### UC16 — Remove Review or Rating

| | |
|---|---|
| **UC Reference Name/Number** | UC16 — Remove Review or Rating |
| **Overview** | The administrator removes a user-submitted review or rating and the system deletes it, so that spam, abusive, or inaccurate content can be taken down. |
| **Related use cases** | None. |
| **Actors** | Administrator |

