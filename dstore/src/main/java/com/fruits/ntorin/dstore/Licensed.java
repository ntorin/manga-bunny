package com.fruits.ntorin.dstore;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Ntori on 8/4/2016.
 */
public class Licensed {
    public static ArrayList<String> getLicensed(){
        Document document = null;
        boolean success = false;
        int tries = 0;
        while (!success && tries++ < 50) {
            try {
                document = Jsoup.connect("http://www.animenewsnetwork.com/encyclopedia/anime-list.php?limit_to=&showG=1&licensed=1&sort=relevance").maxBodySize(0).get();
                success = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //System.out.print(document.html());
        Elements tbodys = document.getElementsByTag("tbody");
        Elements tr = tbodys.get(1).children();
        Elements tr2 = tbodys.get(3).children();
        tr.addAll(tr2);

        ArrayList<String> licensed = new ArrayList<>();
        for(Element e : tr){
            Element a = e.select("td > font > b > a").first();
            String[] check = a.text().split(" ");
            if(!check[check.length - 1].contains("manga")){
                continue;
            }
            try {
                success = false;
                tries = 0;
                Document info = null;
                while (!success && tries++ < 50) {
                    info = Jsoup.connect(a.absUrl("href")).timeout(10000).get();
                    success = true;
                }
                if(info != null) {
                    Element title = info.getElementById("page_header");
                    licensed.add(cleanString(title.text()));
                    //System.out.println("\"" + cleanString(title.text()) + "\",");
                    Element infotype = info.getElementById("infotype-related");
                    if (infotype != null) {
                        Elements ani = infotype.getElementsByTag("a");
                        for (Element aninames : ani) {
                            licensed.add(cleanString(aninames.text()));
                            //System.out.println("\"" + cleanString(aninames.text()) + "\",");
                        }
                    }
                    Element infotype2 = info.getElementById("infotype-2");
                    if (infotype2 != null) {
                        Elements tab = infotype2.getElementsByClass("tab");
                        for (Element altnames : tab) {
                            licensed.add(altnames.text());
                            //System.out.println("\"" + cleanString(altnames.text()) + "\",");
                        }
                    }
                }

            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return licensed;
    }

    public static String cleanString(String s){
        String cleanString = "";
        String[] split = s.split(" ");
        if(split[split.length - 1].startsWith("(")){
            split[split.length - 1] = "";
        }
        for(String frag : split){
            cleanString += frag + " ";
        }
        return cleanString;
    }

}
