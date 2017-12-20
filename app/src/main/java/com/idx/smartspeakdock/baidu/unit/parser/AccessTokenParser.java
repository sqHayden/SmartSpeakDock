/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.idx.smartspeakdock.baidu.unit.parser;

import com.idx.smartspeakdock.baidu.unit.exception.UnitError;
import com.idx.smartspeakdock.baidu.unit.model.AccessToken;

import org.json.JSONException;
import org.json.JSONObject;

public class AccessTokenParser implements Parser<AccessToken> {
    @Override
    public AccessToken parse(String json) throws UnitError {
        try {
            AccessToken accessToken = new AccessToken();
            accessToken.setJson(json);
            JSONObject jsonObject = new JSONObject(json);

            if (jsonObject != null) {

                accessToken.setAccessToken(jsonObject.optString("access_token"));
                accessToken.setExpiresIn(jsonObject.optInt("expires_in"));
                return accessToken;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            UnitError error = new UnitError(UnitError.ErrorCode.JSON_PARSE_ERROR, "Json parse error", e);
            throw error;
        }
        return null;
    }
}
