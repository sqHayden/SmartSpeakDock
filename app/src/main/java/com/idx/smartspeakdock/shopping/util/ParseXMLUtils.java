package com.idx.smartspeakdock.shopping.util;

import android.content.Context;
import android.content.res.XmlResourceParser;

import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.shopping.shoproom.entity.Shopping;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryan on 18-1-31.
 * Email: Ryan_chan01212@yeah.net
 */

public class ParseXMLUtils {

    public static List<Shopping> readXMLPull(Context context){
        XmlResourceParser parser = context.getResources().getXml(R.xml.shopurls);
        try {
            int eventType = parser.getEventType();
            Shopping curr_shopping = null;
            List<Shopping> shoppings = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        shoppings = new ArrayList<>();
                        break;
                    case XmlPullParser.START_TAG:
                        String name = parser.getName();
                        if (name.equalsIgnoreCase("shopping")) {
                            curr_shopping = new Shopping();
//                            curr_shopping.setId(new Integer(parser.getAttributeValue(null, "id")));
                        } else if (curr_shopping != null) {
                            if (name.equalsIgnoreCase("webname")) {
                                // 如果后面是Text元素,即返回它的值
                                curr_shopping.setWebName(parser.nextText());
                            } else if (name.equalsIgnoreCase("weburl")) {
                                curr_shopping.setWebUrl(parser.nextText());
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (parser.getName().equalsIgnoreCase("shopping") && curr_shopping != null) {
                            shoppings.add(curr_shopping);
                            curr_shopping = null;
                        }
                        break;
                }
                eventType = parser.next();
            }
            return shoppings;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
