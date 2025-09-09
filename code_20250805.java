import org.json.JSONObject;
import java.io.File;
import java.net.URLEncoder;
import me.hd.wauxv.plugin.api.callback.PluginCallBack;
// Author: kalicyh
// 由 轩心云 提供接口
//命令都是长按发送
// 命令关键字定义，方便自定义
final String CMD_SET_VOICE = "##音色";  //切换音色命令
final String CMD_LIST_VOICE = "##音色列表";  //查看音色列表命令
final String CMD_TTS = "#";  //转换命令
boolean onLongClickSendBtn(String text) {
    try {
        if (text.startsWith(CMD_SET_VOICE)) {
            if (text.equals(CMD_LIST_VOICE)) {
                sendText(getTargetTalker(), "可选音色示例：\n" +
                        "360 - 雷军\n" +
                        "364 - 蔡徐坤\n" +
                        "365 - 丁真\n" +
                        "370 - 杨幂（女声）\n" +
                        "371 - 赵丽颖（女声）\n" +
                        "372 - 杨紫（女声）\n" +
                        "完整音色列表：https://www.yx520.ltd/API/wzzyy/ys.php/");
                return true;
            } else {
                String numStr = text.replace(CMD_SET_VOICE, "").trim();
                int newVoiceId = Integer.parseInt(numStr);
                putInt("current_voice_id", newVoiceId);
                toast("音色已设置为：" + newVoiceId);
                return true;
            }
        } else if (text.startsWith(CMD_TTS)) {
            String str = text.substring(CMD_TTS.length());
            // 关键修复：对文本进行URL编码，避免特殊字符导致接口错误
            String encodedText = URLEncoder.encode(str, "UTF-8");
            int voiceId = getInt("current_voice_id", 364);
            // 使用编码后的文本拼接URL
            String url = "https://www.yx520.ltd/API/wzzyy/silk.php?text=" + encodedText + "&voice=" + voiceId;
            get(url, null, new PluginCallBack.HttpCallback() {
                public void onSuccess(int respCode, String respContent) {
                    try {
                        JSONObject json = new JSONObject(respContent);
                        String code = json.getString("code");
                        if ("0".equals(code)) {
                            String audioUrl = json.getString("url");
                            // 检查音频链接是否为空
                            if (audioUrl == null || audioUrl.isEmpty()) {
                                toast("接口未返回有效音频链接");
                                return;
                            }
                            download(audioUrl, pluginDir + "/voice.silk", null, new PluginCallBack.DownloadCallback() {
                                public void onSuccess(File file) {
                                    sendVoice(getTargetTalker(), file.getAbsolutePath());
                                }
                                public void onError(Exception e) {
                                    toast("下载异常: " + e.getMessage());
                                }
                            });
                        } else {
                            toast("生成失败，错误码：" + code + "，请检查音色ID是否有效");
                        }
                    } catch (Exception ex) {
                        toast("解析异常: " + ex.toString() + "，响应内容：" + respContent);
                    }
                }
                public void onError(Exception e) {
                    toast("生成异常: " + e.toString());
                }
            });
            return true;
        }
    } catch (Exception e) {
        toast("处理异常: " + e.toString());
    }
    return false;
}
