import {post} from "../method.js"

export async function register(request) {
    return post("api/auth/register",request);
}