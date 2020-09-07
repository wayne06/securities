import {reqRealEnd, reqRealEndAsync} from "./axiosCommon";
import {config} from "./frontConfig";
import store from "../store/index";

export const queryBalance = () => {
    reqRealEndAsync(
        "post", config.real_domain,
        '/api/balance',
        {uid: sessionStorage.getItem('uid')},
        (code, msg, data) => {store.commit("updateBalance", data);}
    );
};

export const queryPosi = () => {
    reqRealEndAsync(
        "post", config.real_domain,
        '/api/posi',
        {uid: sessionStorage.getItem('uid')},
        (code, msg, data) => {store.commit("updatePosi", data)}
    );
};

export const queryOrder = () => {
    reqRealEndAsync(
        "post", config.real_domain,
        "/api/order",
        {uid: sessionStorage.getItem('uid')},
        (code, msg, data) => {store.commit("updateOrder"), data}
    );
};

export const queryTrade = () => {
    reqRealEndAsync(
        "post", config.real_domain,
        "/api/trade",
        {"uid": sessionStorage.getItem('uid')},
        (code, msg, data) => {store.commit('updateTrade'), data}
    );
};

export const queryStock = (params) => {
    return reqRealEnd(
        "post", config.real_domain,
        "/api/stock", params
    );
};

export const sendOrder = (params, callback) => {
    return reqRealEndAsync(
        "post", config.real_domain,
        "/api/sendorder", params, callback
    )
};
