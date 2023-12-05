import requests
import json

# Base URLs for the microservices
SONG_SERVICE_URL = "http://localhost:3001"
PROFILE_SERVICE_URL = "http://localhost:3002"

# Constants for testing
SONG_ID = "5d620f54d78b833e34e65b46"
USERNAME = "test-user-1asdasdjdwsjasd"
FRIEND_USERNAME = "test-user-2" #i had previously created a user with this username


# Helper function to print the test results
def print_result(test_name, response):
    decoded = response.json()
    status = decoded.get('status', 'ERROR')
    message = decoded.get('message', None)
    
    if response.status_code == 200:
        print(f"✅ {test_name} Passed\n\tStatus Code {response.status_code}\n\tStatus: {status}")
    else:
        print(f"❌ {test_name} Failed\n\tStatus Code {response.status_code}\n\tStatus: {status}")

    if message is not None:
        print(f"\tMessage: {message}")
    
    print(json.dumps(decoded, indent=4))
    print("-" * 50)

# Test Cases
test_cases = [
    {
        "name": "Add Song",
        "method": "post",
        "url": f"{SONG_SERVICE_URL}/addSong",
        "data": {
            "songName": "Test Song",
            "songArtistFullName": "Test Artist",
            "songAlbum": "Test Album"
        }
    },
    {
        "name": "Get Song by ID",
        "method": "get",
        "url": f"{SONG_SERVICE_URL}/getSongById/{SONG_ID}"
    },
    {
        "name": "Update Song Favourites Count - Increment",
        "method": "put",
        "url": f"{SONG_SERVICE_URL}/updateSongFavouritesCount",
        "data": {"songId": SONG_ID, "shouldDecrement": False}
    },
    {
        "name": "Update Song Favourites Count - Decrement",
        "method": "put",
        "url": f"{SONG_SERVICE_URL}/updateSongFavouritesCount",
        "data": {"songId": SONG_ID, "shouldDecrement": True}
    },
    {
        "name": "Delete Song by ID",
        "method": "delete",
        "url": f"{SONG_SERVICE_URL}/deleteSongById/{SONG_ID}"
    },
    {
        "name": "Add Profile",
        "method": "post",
        "url": f"{PROFILE_SERVICE_URL}/profile",
        "data": {"userName": USERNAME, "fullName": "Test User", "password": "password"}
    },
    {
        "name": "Follow Friend",
        "method": "put",
        "url": f"{PROFILE_SERVICE_URL}/followFriend",
        "data": {"userName": USERNAME, "friendUserName": FRIEND_USERNAME}
    },
    # {
    #     "name": "Unfollow Friend",
    #     "method": "put",
    #     "url": f"{PROFILE_SERVICE_URL}/unfollowFriend",
    #     "data": {"userName": USERNAME, "friendUserName": FRIEND_USERNAME}
    # },
    {
        "name": "Like Song",
        "method": "put",
        "url": f"{PROFILE_SERVICE_URL}/likeSong",
        "data": {"userName": USERNAME, "songId": SONG_ID}
    },
    {
        "name": "Unlike Song",
        "method": "put",
        "url": f"{PROFILE_SERVICE_URL}/unlikeSong",
        "data": {"userName": USERNAME, "songId": SONG_ID}
    },
    {
        "name": "Get All Friend Favourite Song Titles",
        "method": "get",
        "url": f"{PROFILE_SERVICE_URL}/getAllFriendFavouriteSongTitles/{USERNAME}"
    },
    {
        "name": "Find Trending Songs",
        "method": "get",
        "url": f"{SONG_SERVICE_URL}/trending",
        "params": {"limit": 5}
    },
    {
        "name": "Get Made For You Playlist",
        "method": "get",
        "url": f"{SONG_SERVICE_URL}/madeForYou"
    }
    # Add other specific tests if needed...
]

# Function to run tests
def run_tests():
    print("🔎 Running Tests for Song and Profile Microservices")
    for test in test_cases:
        method = test.get("method")
        url = test.get("url")
        data = test.get("data", {})
        params = test.get("params", {})

        if method == "get":
            response = requests.get(url, params=params)
        elif method == "post":
            response = requests.post(url, json=data)
        elif method == "put":
            response = requests.put(url, json=data)
        elif method == "delete":
            # print("SKIPPING THIS REALLY\n")
            # print("-" * 50)
            # continue
            response = requests.delete(url, json=data)
        
        print_result(test.get("name"), response)

if __name__ == "__main__":
    run_tests()
