import {post} from "../method.js"
export async function login(request) {
    return post(
        "api/auth/login", request
    );
}