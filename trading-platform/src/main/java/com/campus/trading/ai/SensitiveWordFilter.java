package com.campus.trading.ai;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class SensitiveWordFilter {
    private static final List<String> BLOCKED_TERMS = List.of(
            "枪支", "手枪", "步枪", "弹药", "毒品", "冰毒", "海洛因", "大麻",
            "色情", "成人视频", "赌博", "博彩", "赌局", "代写", "代考",
            "包治百病", "绝对正品", "假一赔十",
            "加微信", "加微", "私聊优惠", "扫二维码", "扫码联系"
    );
    private static final Pattern PHONE_NUMBER =
            Pattern.compile("(?<!\\d)(?:\\+?86[-\\s]?)?1[3-9]\\d{9}(?!\\d)");
    private static final Pattern WECHAT_ID =
            Pattern.compile("(?i)(?:微信(?:号)?|wechat|wxid_)[\\s:：_-]*[a-z][-_a-z0-9]{5,19}");
    private static final Pattern QQ_ID =
            Pattern.compile("(?i)(?:QQ(?:号)?|企鹅号)[\\s:：_-]*\\d{5,12}");

    public String findViolation(String title, String description) {
        String content = ((title == null ? "" : title) + "\n"
                + (description == null ? "" : description)).toLowerCase(Locale.ROOT);
        for (String term : BLOCKED_TERMS) {
            if (content.contains(term.toLowerCase(Locale.ROOT))) {
                return "命中违禁或导流词：" + term;
            }
        }
        if (PHONE_NUMBER.matcher(description == null ? "" : description).find()) {
            return "商品描述包含手机号";
        }
        if (WECHAT_ID.matcher(description == null ? "" : description).find()) {
            return "商品描述包含微信号";
        }
        if (QQ_ID.matcher(description == null ? "" : description).find()) {
            return "商品描述包含 QQ 号";
        }
        return null;
    }
}
