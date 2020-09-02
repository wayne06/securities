import {reqRealEnd, reqRealEndAsync} from './axiosCommon'

import {config} from "./frontConfig";
import router from "../router";

export const queryCaptcha = (callback) => {
    return reqRealEndAsync("post", config.real_domain, "/login/captcha", {}, callback);
};

export const login = (params, callback) => {
    return reqRealEndAsync("post", config.real_domain, "/login/userlogin", params, callback);
};

export const logout = () => {
    sessionStorage.removeItem("uid");
    sessionStorage.removeItem("token");
    router.replace({
        path: "/",
        query: {msg: "成功退出"}
    });
    reqRealEnd("post", config.real_domain, "/login/logout", {});
};

export const updatePass = (params, callback) => {
    return reqRealEndAsync("post", config.real_domain, "/login/changepass", params, callback);
};
