package UI;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.prefs.Preferences;

public final class PlannerStore {
    private static final Preferences P=Preferences.userRoot().node("lifetracker/planner");
    private PlannerStore(){}
    public record Assignment(String id,String title,String className,LocalDate due,String points,boolean completed){}
    private static String key(String user,String suffix){return user.replaceAll("[^A-Za-z0-9_.-]","_")+"."+suffix;}
    private static String enc(String s){return Base64.getUrlEncoder().withoutPadding().encodeToString((s==null?"":s).getBytes(StandardCharsets.UTF_8));}
    private static String dec(String s){try{return new String(Base64.getUrlDecoder().decode(s),StandardCharsets.UTF_8);}catch(Exception e){return "";}}
    public static List<Assignment> loadAssignments(String user){
        List<Assignment> out=new ArrayList<>();String raw=P.get(key(user,"assignments"),"");if(raw.isBlank())return out;
        for(String line:raw.split("\\n")){String[] a=line.split("\\|",-1);try{if(a.length>=6)out.add(new Assignment(a[0],dec(a[1]),dec(a[2]),LocalDate.parse(a[3]),dec(a[4]),Boolean.parseBoolean(a[5])));}catch(Exception ignored){}}
        out.sort(Comparator.comparing(Assignment::due));return out;
    }
    public static void saveAssignments(String user,List<Assignment> items){StringBuilder b=new StringBuilder();for(Assignment a:items)b.append(a.id()).append('|').append(enc(a.title())).append('|').append(enc(a.className())).append('|').append(a.due()).append('|').append(enc(a.points())).append('|').append(a.completed()).append('\n');P.put(key(user,"assignments"),b.toString());}
    public static int getSatTarget(String u){return P.getInt(key(u,"satTarget"),0);} public static void setSatTarget(String u,int v){P.putInt(key(u,"satTarget"),v);}
    public static int getSatCurrent(String u){return P.getInt(key(u,"satCurrent"),0);} public static void setSatCurrent(String u,int v){P.putInt(key(u,"satCurrent"),v);}
    public static int getSatMath(String u){return P.getInt(key(u,"satMath"),0);} public static void setSatMath(String u,int v){P.putInt(key(u,"satMath"),v);}
    public static int getSatRW(String u){return P.getInt(key(u,"satRW"),0);} public static void setSatRW(String u,int v){P.putInt(key(u,"satRW"),v);}
    public static String getSatDate(String u){return P.get(key(u,"satDate"),"");} public static void setSatDate(String u,String v){P.put(key(u,"satDate"),v==null?"":v);}
}
