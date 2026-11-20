import requests

API = "http://localhost:8080"

def login(username, password):
    r = requests.post(f"{API}/api/auth/login", json={"username": username, "password": password})
    r.raise_for_status()
    return r.json()["token"]

TOKEN_ALICE = login("alice", "alice123")
print("Token alice :", TOKEN_ALICE)

