package com.joejoe2.testsensor.edutalk;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class EduTalkRCConfig {
    public String device_name, device_model, idf_list, csm_url, rc_bind;

    EduTalkRCConfig(String htmlResponse){
        Document document=Jsoup.parse(htmlResponse);
        Element scriptElement=document.getElementsByTag("body").get(0).getElementsByTag("script").get(0);
        String[] lines = scriptElement.data().replaceAll("\\s+", "").split(";");
        for (String line : lines) {
            if(line.startsWith("vardev")){
                device_name = line.substring(line.indexOf("\"")+1, line.length()-1);
            }else if(line.startsWith("vardm_name")){
                device_model = line.substring(line.indexOf("\"")+1, line.length()-1);
            }else if(line.startsWith("varidf_list")){
                idf_list = line.substring(line.indexOf("=")+1);
            }else if(line.startsWith("varurls")){
                String[] urls = line.split(",");
                csm_url = urls[0].substring(urls[0].indexOf("csm_url:'")+9, urls[0].length()-1);
                rc_bind = urls[1].substring(urls[1].indexOf("rc_bind:function(id){return'")+29, urls[1].lastIndexOf("'"));
                rc_bind = csm_url.substring(0, csm_url.indexOf("//")+2)+csm_url.split("/")[2]+"/"+rc_bind;
            }
        }
    }
}
