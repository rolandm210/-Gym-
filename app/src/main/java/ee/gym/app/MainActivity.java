package ee.gym.app;

import android.app.*;
import android.os.Bundle;
import android.graphics.*;
import android.graphics.drawable.*;
import android.content.*;
import android.view.*;
import android.widget.*;
import android.text.InputType;
import org.json.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class MainActivity extends Activity {
  private final int BG=0xff0D1110,CARD=0xff1B2420,ACCENT=0xffC8FF4D,WHITE=0xffF2F7F1,MUTED=0xff99A99F,LINE=0xff344238;
  private LinearLayout content,root;
  private String tab="Kodu";
  private android.content.SharedPreferences prefs;
  private final String[] muscleGroups={"Rind","Selg","Õlad","Biitseps","Triitseps","Jalad","Kõht"};
  private final String[][] exerciseGroups={
    {"Bench press","Incline dumbbell press","Incline smith press","Machine chest press","Cable fly","Pec deck","Push-ups","Dips","Dumbbell fly","Decline bench press"},
    {"Pull-ups","Lat pulldown","Close grip pulldown","Barbell row","T-bar row","Seated cable row","Single arm dumbbell row","Chest supported row","Deadlift","Straight arm pulldown"},
    {"Shoulder press","Dumbbell shoulder press","Lateral raise","Cable lateral raise","Reverse pec deck","Face pull","Rear delt fly","Overhead press","Front raise"},
    {"Preacher curl","Incline dumbbell curl","Bayesian curl","Hammer curl","EZ-bar curl","Cable curl","Barbell curl","Concentration curl","Reverse curl"},
    {"Tricep pushdown","Rope pushdown","Overhead cable extension","Skull crushers","Close grip bench press","Single arm extension","Tricep kickbacks","Bench dips"},
    {"Squat","Hack squat","Leg press","Leg extension","Lying leg curl","Seated leg curl","Romanian deadlift","Bulgarian split squat","Lunges","Hip thrust","Calf raises","Hip abduction"},
    {"Cable crunch","Decline crunch","Hanging leg raises","Plank","Ab wheel","Reverse crunch","Bicycle crunch","Russian twist"}
  };
  private final String[] measurements={"Rind","Vöökoht","Puus","Õlad","Kael","Biitseps vasak","Biitseps parem","Küünarvars","Reis vasak","Reis parem","Säär vasak","Säär parem"};
  private final String[] prNames={"Bench press","Squat","Deadlift","Pull-ups","Shoulder press","Leg press","Lat pulldown","Barbell row","Dips"};

  @Override public void onCreate(Bundle state){
    super.onCreate(state);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);
    prefs=getSharedPreferences("gym_data_v1",MODE_PRIVATE);
    if(!prefs.contains("plans")) seedPlans();
    show();
  }
  private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}
  private GradientDrawable round(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
  private TextView text(String value,int size,int color,boolean bold){
    TextView t=new TextView(this);t.setText(value);t.setTextColor(color);t.setTextSize(size);
    t.setPadding(dp(2),dp(7),dp(2),dp(7));if(bold)t.setTypeface(null,1);return t;
  }
  private LinearLayout col(){LinearLayout c=new LinearLayout(this);c.setOrientation(1);return c;}
  private LinearLayout panel(){
    LinearLayout c=col();c.setPadding(dp(16),dp(12),dp(16),dp(14));c.setBackground(round(CARD,19));
    LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(7),0,dp(8));content.addView(c,p);return c;
  }
  private TextView title(String s){TextView t=text(s,25,WHITE,true);content.addView(t);return t;}
  private void subtitle(String s){content.addView(text(s,13,MUTED,false));}
  private void section(String s){TextView t=text(s,17,WHITE,true);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(12);content.addView(t,p);}
  private void notice(String name,String detail){LinearLayout c=panel();c.addView(text(name,18,ACCENT,true));c.addView(text(detail,14,WHITE,false));}
  private void action(LinearLayout where,String label,Runnable r,boolean primary){
    TextView b=text(label,15,primary?BG:ACCENT,true);b.setGravity(Gravity.CENTER);
    b.setPadding(dp(12),dp(12),dp(12),dp(12));b.setMinHeight(dp(47));
    b.setBackground(round(primary?ACCENT:0xff2A382E,13));
    LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(6),0,dp(5));
    where.addView(b,p);b.setOnClickListener(v->r.run());
  }
  private void btn(String label,Runnable r){action(content,label,r,true);}
  private void small(LinearLayout where,String label,Runnable r){action(where,label,r,false);}
  private EditText input(LinearLayout parent,String hint,String value,boolean number){
    EditText e=new EditText(this);e.setSingleLine(true);e.setTextColor(WHITE);e.setHintTextColor(MUTED);e.setHint(hint);
    e.setTextSize(15);e.setText(value==null?"":value);e.setPadding(dp(12),dp(12),dp(12),dp(12));
    e.setBackground(round(0xff26332C,12));
    if(number)e.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_NUMBER_FLAG_SIGNED);
    LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(4),0,dp(6));parent.addView(e,p);
    return e;
  }
  private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
  private JSONObject parse(String s){try{return new JSONObject(s);}catch(Exception ex){return new JSONObject();}}
  private JSONArray array(String s){try{return new JSONArray(s);}catch(Exception ex){return new JSONArray();}}
  private JSONObject obj(JSONArray a,int i){JSONObject o=a.optJSONObject(i);return o==null?new JSONObject():o;}
  private void put(JSONObject o,String key,Object v){try{o.put(key,v);}catch(Exception ignored){}}
  private JSONObject json(String key){return parse(prefs.getString(key,"{}"));}
  private JSONArray list(String key){return array(prefs.getString(key,"[]"));}
  private void save(String key,Object obj){prefs.edit().putString(key,obj.toString()).apply();}
  private String today(long when){return new SimpleDateFormat("dd.MM.yyyy",Locale.getDefault()).format(new Date(when));}
  private String value(EditText e){return e.getText().toString().trim();}
  private double decimal(String s){try{return Double.parseDouble(s.trim().replace(',','.'));}catch(Exception ex){return -1;}}
  private void confirm(String title,String message,Runnable ok){new AlertDialog.Builder(this).setTitle(title).setMessage(message)
    .setNegativeButton("Tühista",null).setPositiveButton("Kinnita",(d,w)->ok.run()).show();}
  private void ask(String heading,String hint,String initial,java.util.function.Consumer<String> result){
    EditText e=new EditText(this);e.setSingleLine(true);e.setText(initial);e.setSelectAllOnFocus(true);
    e.setPadding(dp(18),dp(16),dp(18),dp(16));e.setHint(hint);
    new AlertDialog.Builder(this).setTitle(heading).setView(e).setNegativeButton("Tühista",null)
      .setPositiveButton("Salvesta",(d,w)->{String s=value(e);if(!s.isEmpty())result.accept(s);else toast("Väli on tühi");}).show();
  }
  private void show(){
    root=col();root.setBackgroundColor(BG);setContentView(root);
    LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);header.setPadding(dp(20),dp(12),dp(20),dp(9));root.addView(header);
    ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.gym_logo);header.addView(logo,new LinearLayout.LayoutParams(dp(56),dp(56)));
    LinearLayout brand=col();header.addView(brand);brand.addView(text("/Gym\\",27,ACCENT,true));brand.addView(text("TRAIN · TRACK · PROGRESS",10,MUTED,true));
    ScrollView scroll=new ScrollView(this);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    content=col();content.setPadding(dp(18),dp(8),dp(18),dp(28));scroll.addView(content);
    if(tab.equals("Kodu"))home();else if(tab.equals("Kavad"))plans();else if(tab.equals("Treening"))workout();
    else if(tab.equals("Progress"))progress();else history();
    LinearLayout nav=new LinearLayout(this);nav.setBackgroundColor(CARD);nav.setPadding(dp(2),dp(6),dp(2),dp(8));root.addView(nav);
    String[] tabs={"Kodu","Kavad","Treening","Progress","Ajalugu"},icons={"⌂","▤","✚","▥","◷"};
    for(int i=0;i<tabs.length;i++){
      final String name=tabs[i];LinearLayout cell=col();cell.setGravity(Gravity.CENTER);nav.addView(cell,new LinearLayout.LayoutParams(0,dp(64),1));
      TextView icon=text(icons[i],24,name.equals(tab)?ACCENT:MUTED,true);icon.setPadding(0,0,0,0);icon.setGravity(Gravity.CENTER);cell.addView(icon);
      TextView label=text(name,10,name.equals(tab)?ACCENT:MUTED,false);label.setPadding(0,0,0,0);label.setGravity(Gravity.CENTER);cell.addView(label);
      cell.setOnClickListener(v->{tab=name;show();});
    }
  }
  private void go(String name){tab=name;show();}
  private JSONObject newExercise(String name){
    JSONObject e=new JSONObject();put(e,"name",name);put(e,"sets",3);put(e,"reps","8–12");put(e,"note","");return e;
  }
  private JSONObject newDay(String name,String... exercises){
    JSONObject day=new JSONObject();put(day,"name",name);JSONArray a=new JSONArray();for(String e:exercises)a.put(newExercise(e));put(day,"exercises",a);return day;
  }
  private JSONObject newPlan(String name){
    JSONObject p=new JSONObject();put(p,"id",System.currentTimeMillis()+new Random().nextInt(1000));put(p,"name",name);put(p,"days",new JSONArray());return p;
  }
  private void seedPlans(){
    JSONArray all=new JSONArray();JSONObject plan=newPlan("GBGB · 5 päeva");JSONArray days=new JSONArray();
    days.put(gbgbDay("1. päev · rind ja biitseps",
      new String[]{"Pec deck","Dips","Preacher curls","Dumbbell incline curls","Cable lateral raise"},
      new String[]{"8-10","failure (max 12)","10-12","10-12","10-15"},
      new String[]{"Hoia käed sirged","Kui alla 5 korduse, kasuta abikummi; üle 12 lisa raskust","Rohkem kordusi või suurem raskus","",""}));
    days.put(gbgbDay("2. päev · selg ja triitseps",
      new String[]{"Lat pulldown / Pull-ups","T-bar row / Cable row","Tricep pushdown","Tricep extension"},
      new String[]{"","", "8-10","8-10"},
      new String[]{"Jälgi tehnikat","Tõmba lõpuni taha ja lase ette välja","",""}));
    days.put(gbgbDay("3. päev · jalad, kõht ja küünarvars",
      new String[]{"Leg extension","Lying leg curl","Leg press","Reverse curl","Decline crunch / Captain's chair leg raises"},
      new String[]{"10-12","10-12","8-10","failure","failure"},
      new String[]{"","","","Vähemalt 10 kordust","Üle 25 korduse korral lisa raskust"}));
    days.put(gbgbDay("4. päev · rind ja biitseps",
      new String[]{"Bench press","Dumbbell incline bench press / Smith incline bench press","Bayesian curls","Hammer curls / Preacher curls","Seated dumbbell shoulder press"},
      new String[]{"8-10","8-10","8-10","8-10","8-10"},
      new String[]{"","","","Kaabliga alternatiiv sobib samuti",""}));
    days.put(gbgbDay("5. päev · selg ja triitseps",
      new String[]{"Lat pulldown / Pull-ups","T-bar row / Cable row","Tricep pushdown","Tricep extension"},
      new String[]{"","","8-10","8-10"},
      new String[]{"Jälgi tehnikat","Tõmba lõpuni taha ja lase ette välja","",""}));
    put(plan,"days",days);all.put(plan);save("plans",all);prefs.edit().putInt("selected_plan",0).apply();
  }
  private JSONObject gbgbDay(String name,String[] names,String[] reps,String[] notes){
    JSONObject day=newDay(name);JSONArray exercises=day.optJSONArray("exercises");
    for(int i=0;i<names.length;i++){
      JSONObject e=newExercise(names[i]);put(e,"sets",3);put(e,"reps",reps[i].isEmpty()?"Vali jõukohane korduste arv":reps[i]);
      put(e,"note",notes[i]);exercises.put(e);
    }
    return day;
  }
  private void installGbgbPlan(){
    JSONArray plans=list("plans");for(int i=0;i<plans.length();i++)if("GBGB · 5 päeva".equals(obj(plans,i).optString("name"))){prefs.edit().putInt("selected_plan",i).apply();go("Kavad");return;}
    String existing=prefs.getString("plans","[]");seedPlans();JSONArray seeded=list("plans");JSONArray merged=array(existing);
    merged.put(obj(seeded,0));save("plans",merged);prefs.edit().putInt("selected_plan",merged.length()-1).apply();go("Kavad");
  }
  private JSONObject plan(){JSONArray a=list("plans");return obj(a,Math.min(Math.max(0,prefs.getInt("selected_plan",0)),Math.max(0,a.length()-1)));}
  private int planIndex(){JSONArray a=list("plans");return Math.min(Math.max(0,prefs.getInt("selected_plan",0)),Math.max(0,a.length()-1));}
  private void persistPlan(JSONObject p){
    JSONArray plans=list("plans");int index=planIndex();try{plans.put(index,p);}catch(Exception ignored){}save("plans",plans);
  }
  private void metricTile(LinearLayout row,String amount,String caption){
    LinearLayout tile=col();tile.setPadding(dp(13),dp(14),dp(10),dp(14));tile.setBackground(round(CARD,16));
    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(tile,lp);
    tile.addView(text(amount,24,ACCENT,true));tile.addView(text(caption,12,MUTED,false));
  }
  private void home(){
    title("Ülevaade");
    JSONArray history=list("workouts");JSONObject metrics=json("metrics");
    LinearLayout r=new LinearLayout(this);content.addView(r);metricTile(r,""+history.length(),"Treeninguid");metricTile(r,metrics.optString("weight","–")+" kg","Kehakaal");
    LinearLayout rr=new LinearLayout(this);content.addView(rr);metricTile(rr,""+list("plans").length(),"Treeningkavasid");
    metricTile(rr,""+countSets(history),"Salvestatud seeriaid");
    JSONObject active=json("active_session");boolean running=active.has("started");

    btn(running?"Jätka treeningut →":"Alusta treeningut →",()->go("Treening"));
    section("Kiirvalikud");LinearLayout card=panel();small(card,"▤   Halda treeningkavasid",()->go("Kavad"));small(card,"▥   Sisesta ümbermõõdud ja PR-id",()->go("Progress"));small(card,"◷   Vaata treeningajalugu",()->go("Ajalugu"));
    section("Viimane treening");
    if(history.length()>0){JSONObject last=obj(history,history.length()-1);notice(last.optString("day","Treening"),today(last.optLong("ended",0))+" · "+last.optJSONArray("exercises").length()+" harjutust");}

  }
  private int countSets(JSONArray sessions){
    int sets=0;for(int i=0;i<sessions.length();i++){JSONArray es=obj(sessions,i).optJSONArray("exercises");if(es==null)continue;
      for(int j=0;j<es.length();j++){JSONArray ss=obj(es,j).optJSONArray("sets");if(ss!=null)sets+=ss.length();}}
    return sets;
  }
  private void plans(){
    title("Minu kavad");
    JSONArray plans=list("plans");
    for(int i=0;i<plans.length();i++){
      final int index=i;JSONObject p=obj(plans,i);LinearLayout c=panel();
      c.addView(text((i==planIndex()?"●  ":"○  ")+p.optString("name"),19,i==planIndex()?ACCENT:WHITE,true));
      JSONArray days=p.optJSONArray("days");c.addView(text((days==null?0:days.length())+" treeningpäeva",13,MUTED,false));
      small(c,i==planIndex()?"Muuda kava  →":"Vali ja muuda  →",()->{prefs.edit().putInt("selected_plan",index).apply();planEditor();});
      if(plans.length()>1)small(c,"Kustuta kava",()->confirm("Kustuta kava?","Kustutatud kava ei saa taastada.",()->{
        JSONArray all=list("plans"),next=new JSONArray();for(int n=0;n<all.length();n++)if(n!=index)next.put(obj(all,n));
        save("plans",next);prefs.edit().putInt("selected_plan",0).apply();show();
      }));
    }
    btn("＋ Lisa GBGB 5-päevane kava",this::installGbgbPlan);
    btn("＋ Loo uus treeningkava",()->ask("Uue kava nimi","Näiteks Upper / Lower","",name->{
      JSONArray all=list("plans");all.put(newPlan(name));save("plans",all);prefs.edit().putInt("selected_plan",all.length()-1).apply();planEditor();
    }));
  }
  private void planEditor(){
    content.removeAllViews();JSONObject p=plan();title(p.optString("name"));subtitle("Muuda treeningpäevi, harjutusi ja eesmärke.");
    btn("← Kõik kavad",()->go("Kavad"));
    btn("✎ Muuda kava nime",()->ask("Kava nimi","Kava nimi",p.optString("name"),name->{put(p,"name",name);persistPlan(p);planEditor();}));
    JSONArray days=p.optJSONArray("days");if(days==null){days=new JSONArray();put(p,"days",days);}
    for(int i=0;i<days.length();i++){
      int idx=i;JSONObject day=obj(days,i);LinearLayout c=panel();
      c.addView(text((i+1)+". päev · "+day.optString("name"),19,ACCENT,true));
      JSONArray ex=day.optJSONArray("exercises");c.addView(text((ex==null?0:ex.length())+" harjutust",13,MUTED,false));
      small(c,"Ava päev →",()->dayEditor(idx));
      small(c,"Muuda päeva nime",()->ask("Päeva nimi","Näiteks Push",day.optString("name"),name->{put(day,"name",name);persistPlan(p);planEditor();}));
      small(c,"Kustuta päev",()->confirm("Kustuta treeningpäev?",day.optString("name"),()->{
        JSONArray old=p.optJSONArray("days"),updated=new JSONArray();for(int j=0;j<old.length();j++)if(j!=idx)updated.put(obj(old,j));
        put(p,"days",updated);persistPlan(p);planEditor();
      }));
    }
    btn("＋ Lisa treeningpäev",()->ask("Uus treeningpäev","Näiteks Upper body","",name->{
      p.optJSONArray("days").put(newDay(name));persistPlan(p);planEditor();
    }));
  }
  private void dayEditor(int index){
    JSONObject p=plan();JSONArray days=p.optJSONArray("days");if(days==null||index>=days.length()){planEditor();return;}
    JSONObject day=obj(days,index);content.removeAllViews();title(day.optString("name"));subtitle(p.optString("name")+" · Harjutused");btn("← Tagasi kava juurde",this::planEditor);
    JSONArray ex=day.optJSONArray("exercises");if(ex==null){ex=new JSONArray();put(day,"exercises",ex);}
    for(int i=0;i<ex.length();i++){
      int idx=i;JSONObject e=obj(ex,i);LinearLayout c=panel();c.addView(text(e.optString("name"),18,WHITE,true));
      c.addView(text(e.optInt("sets",3)+" seeriat × "+e.optString("reps","8–12")+" kordust",13,ACCENT,false));
      if(!e.optString("note").isEmpty())c.addView(text("Märkus: "+e.optString("note"),12,MUTED,false));
      small(c,"✎ Muuda harjutust",()->editExercise(index,idx));
      small(c,"Eemalda",()->confirm("Eemalda harjutus?",e.optString("name"),()->{
        JSONArray updated=new JSONArray();JSONArray orig=day.optJSONArray("exercises");
        for(int j=0;j<orig.length();j++)if(j!=idx)updated.put(obj(orig,j));
        put(day,"exercises",updated);persistPlan(p);dayEditor(index);
      }));
    }
    btn("＋ Lisa harjutus kataloogist",()->pickExercise(name->{
      day.optJSONArray("exercises").put(newExercise(name));persistPlan(p);dayEditor(index);
    }));
    btn("＋ Lisa oma harjutus",()->ask("Harjutuse nimi","Näiteks My custom lift","",name->{
      day.optJSONArray("exercises").put(newExercise(name));persistPlan(p);dayEditor(index);
    }));
  }
  private void editExercise(int dayIndex,int exIndex){
    JSONObject p=plan(),day=obj(p.optJSONArray("days"),dayIndex),e=obj(day.optJSONArray("exercises"),exIndex);
    content.removeAllViews();title("Muuda harjutust");subtitle(day.optString("name"));btn("← Tagasi",()->dayEditor(dayIndex));
    EditText name=input(content,"Harjutuse nimi",e.optString("name"),false);
    EditText sets=input(content,"Seeriate arv",""+e.optInt("sets",3),true);
    EditText reps=input(content,"Kordused, nt 8–12",e.optString("reps","8–12"),false);
    EditText note=input(content,"Märkus",e.optString("note",""),false);
    btn("Salvesta harjutus",()->{
      int count;try{count=Integer.parseInt(value(sets));}catch(Exception ex){toast("Vigane seeriate arv");return;}
      if(count<1||count>30||value(name).isEmpty()){toast("Kontrolli harjutuse nime ja seeriate arvu");return;}
      put(e,"name",value(name));put(e,"sets",count);put(e,"reps",value(reps));put(e,"note",value(note));persistPlan(p);dayEditor(dayIndex);
    });
  }
  private void pickExercise(java.util.function.Consumer<String> callback){
    new AlertDialog.Builder(this).setTitle("Vali lihasgrupp").setItems(muscleGroups,(d,group)->{
      new AlertDialog.Builder(this).setTitle(muscleGroups[group]).setItems(exerciseGroups[group],(dialog,which)->callback.accept(exerciseGroups[group][which]))
        .setNegativeButton("Tagasi",null).show();
    }).setNegativeButton("Sulge",null).show();
  }
  private void workout(){
    JSONObject session=json("active_session");
    if(session.has("started")){activeWorkout(session);return;}
    title("Treening");
    JSONObject p=plan();JSONArray days=p.optJSONArray("days");
    section("Aktiivne kava · "+p.optString("name"));
    if(days==null||days.length()==0){notice("Kava on tühi","Lisa kõigepealt treeningkavale päev ja harjutused.");btn("Muuda kava",this::planEditor);return;}
    for(int i=0;i<days.length();i++){
      int index=i;JSONObject day=obj(days,i);LinearLayout c=panel();c.addView(text((i+1)+". päev · "+day.optString("name"),18,WHITE,true));
      JSONArray ex=day.optJSONArray("exercises");c.addView(text((ex==null?0:ex.length())+" harjutust",13,MUTED,false));
      small(c,"Alusta seda päeva →",()->startWorkout(index));
    }
    btn("Halda kavasid",()->go("Kavad"));
  }
  private void startWorkout(int dayIndex){
    JSONObject p=plan(),day=obj(p.optJSONArray("days"),dayIndex);
    JSONObject session=new JSONObject();put(session,"started",System.currentTimeMillis());put(session,"plan",p.optString("name"));put(session,"day",day.optString("name"));
    JSONArray result=new JSONArray(),ex=day.optJSONArray("exercises");if(ex!=null)for(int i=0;i<ex.length();i++){
      JSONObject source=obj(ex,i),entry=new JSONObject();put(entry,"name",source.optString("name"));put(entry,"note",source.optString("note"));put(entry,"targetSets",source.optInt("sets",3));
      put(entry,"targetReps",source.optString("reps","8–12"));put(entry,"sets",new JSONArray());result.put(entry);
    }
    put(session,"exercises",result);save("active_session",session);show();
  }
  private void activeWorkout(JSONObject session){
    title(session.optString("day","Treening"));subtitle(session.optString("plan")+" · Seeriad salvestuvad automaatselt");
    JSONArray exercises=session.optJSONArray("exercises");if(exercises==null)exercises=new JSONArray();
    int total=0;double volume=0;
    for(int i=0;i<exercises.length();i++){
      JSONArray sets=obj(exercises,i).optJSONArray("sets");if(sets==null)continue;
      total+=sets.length();for(int j=0;j<sets.length();j++){JSONObject set=obj(sets,j);volume+=set.optDouble("weight",0)*set.optInt("reps",0);}
    }
    LinearLayout stats=new LinearLayout(this);content.addView(stats);metricTile(stats,""+total,"Seeriaid");metricTile(stats,""+Math.round(volume)+" kg","Treeningmaht");
    for(int i=0;i<exercises.length();i++){
      final int index=i;JSONObject exercise=obj(exercises,i);String name=exercise.optString("name");
      LinearLayout c=panel();c.addView(text(name,18,WHITE,true));c.addView(text("Eesmärk: "+exercise.optInt("targetSets",3)+" × "+exercise.optString("targetReps","8–12"),13,MUTED,false));
      if(!exercise.optString("note").isEmpty())c.addView(text(exercise.optString("note"),13,ACCENT,false));
      String[] options=name.split(" / ");if(options.length>1)small(c,"Vaheta harjutust  ▾",()->new AlertDialog.Builder(this).setTitle("Vali harjutus").setItems(options,(d,w)->{
        put(exercise,"name",options[w].trim());save("active_session",session);show();
      }).show());
      JSONArray sets=exercise.optJSONArray("sets");if(sets==null){sets=new JSONArray();put(exercise,"sets",sets);}
      for(int k=0;k<sets.length();k++){
        int setIndex=k;JSONObject saved=obj(sets,k);
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);c.addView(row);
        TextView t=text((k+1)+".  "+saved.optString("weight")+" kg × "+saved.optInt("reps"),14,WHITE,false);row.addView(t,new LinearLayout.LayoutParams(0,-2,1));
        TextView remove=text("✕",19,MUTED,true);row.addView(remove);remove.setOnClickListener(v->{
          JSONArray kept=new JSONArray(),old=exercise.optJSONArray("sets");
          for(int n=0;n<old.length();n++)if(n!=setIndex)kept.put(obj(old,n));put(exercise,"sets",kept);save("active_session",session);show();
        });
      }
      LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);c.addView(row);
      EditText kg=input(row,"kg","",true);EditText reps=input(row,"kordused","",true);
      LinearLayout.LayoutParams kp=(LinearLayout.LayoutParams)kg.getLayoutParams();kp.width=0;kp.weight=1;kg.setLayoutParams(kp);
      LinearLayout.LayoutParams rp=(LinearLayout.LayoutParams)reps.getLayoutParams();rp.width=0;rp.weight=1;reps.setLayoutParams(rp);
      small(c,"＋ Salvesta seeria",()->{
        double w=decimal(value(kg));int r;try{r=Integer.parseInt(value(reps));}catch(Exception e){toast("Sisesta kordused");return;}
        if(w<0||r<=0||r>999){toast("Kontrolli raskust ja kordusi");return;}
        JSONObject set=new JSONObject();put(set,"weight",w);put(set,"reps",r);
        exercise.optJSONArray("sets").put(set);save("active_session",session);show();
      });
    }
    btn("＋ Lisa harjutus",()->pickExercise(name->{
      JSONObject e=newExercise(name);put(e,"targetSets",3);put(e,"targetReps","8–12");put(e,"sets",new JSONArray());
      session.optJSONArray("exercises").put(e);save("active_session",session);show();
    }));
    btn("✓ Lõpeta ja salvesta treening",()->{
      if(countSessionSets(session)==0){toast("Lisa esmalt vähemalt üks seeria");return;}
      confirm("Lõpeta treening?","Treening lisatakse ajalukku.",()->{
        put(session,"ended",System.currentTimeMillis());JSONArray workouts=list("workouts");workouts.put(session);save("workouts",workouts);
        prefs.edit().remove("active_session").apply();go("Ajalugu");
      });
    });
    small(content,"Loobu treeningust",()->confirm("Loobu treeningust?","Pooleliolevad seeriad kustutatakse.",()->{
      prefs.edit().remove("active_session").apply();show();
    }));
  }
  private int countSessionSets(JSONObject session){
    JSONArray es=session.optJSONArray("exercises");int n=0;if(es!=null)for(int i=0;i<es.length();i++){JSONArray sets=obj(es,i).optJSONArray("sets");if(sets!=null)n+=sets.length();}return n;
  }
  private void history(){
    title("Treeningajalugu");
    JSONArray workouts=list("workouts");if(workouts.length()==0){notice("Ajalugu on tühi","Salvesta esimene treening, et näha tulemusi.");return;}
    LinkedHashMap<String,ArrayList<JSONObject>> groups=new LinkedHashMap<>();
    for(int i=workouts.length()-1;i>=0;i--){JSONObject session=obj(workouts,i);String date=today(session.optLong("ended",0));groups.computeIfAbsent(date,k->new ArrayList<>()).add(session);}
    for(Map.Entry<String,ArrayList<JSONObject>> day:groups.entrySet()){
      section(day.getKey());for(JSONObject session:day.getValue()){
        LinearLayout c=panel();c.addView(text(session.optString("day","Treening"),19,ACCENT,true));
        c.addView(text(session.optString("plan","")+" · "+countSessionSets(session)+" seeriat",13,MUTED,false));
        JSONArray exercises=session.optJSONArray("exercises");if(exercises==null)continue;
        for(int j=0;j<exercises.length();j++){
          JSONObject e=obj(exercises,j);JSONArray sets=e.optJSONArray("sets");if(sets==null||sets.length()==0)continue;
          c.addView(text(e.optString("name"),15,WHITE,true));
          StringBuilder log=new StringBuilder();for(int k=0;k<sets.length();k++){JSONObject set=obj(sets,k);
            if(k>0)log.append("  ·  ");log.append(set.optDouble("weight")).append(" kg × ").append(set.optInt("reps"));}
          c.addView(text(log.toString(),13,MUTED,false));
        }
        small(c,"Vaata jõugraafikuid",()->exerciseCharts(session));
      }
    }
  }
  private void exerciseCharts(JSONObject session){
    JSONArray es=session.optJSONArray("exercises");if(es==null)return;
    ArrayList<String> names=new ArrayList<>();for(int i=0;i<es.length();i++)if(obj(es,i).optJSONArray("sets")!=null&&!names.contains(obj(es,i).optString("name")))names.add(obj(es,i).optString("name"));
    new AlertDialog.Builder(this).setTitle("Harjutuse graafik").setItems(names.toArray(new String[0]),(d,w)->showChart(names.get(w))).setNegativeButton("Sulge",null).show();
  }
  private void showChart(String exercise){
    content.removeAllViews();title(exercise);subtitle("Suurim tööraskus treeningute lõikes");btn("← Ajalugu",()->go("Ajalugu"));
    JSONArray history=list("workouts");ArrayList<Float> vals=new ArrayList<>();ArrayList<String> dates=new ArrayList<>();
    for(int i=0;i<history.length();i++){JSONObject session=obj(history,i);JSONArray ex=session.optJSONArray("exercises");if(ex==null)continue;
      float best=-1;for(int j=0;j<ex.length();j++){JSONObject e=obj(ex,j);if(!e.optString("name").equalsIgnoreCase(exercise))continue;
        JSONArray sets=e.optJSONArray("sets");if(sets!=null)for(int k=0;k<sets.length();k++)best=Math.max(best,(float)obj(sets,k).optDouble("weight"));}
      if(best>=0){vals.add(best);dates.add(today(session.optLong("ended",0)));}
    }
    if(vals.isEmpty()){notice("Graafik puudub","Selle harjutuse seeriaid pole veel salvestatud.");return;}
    notice("Parim tulemus",Collections.max(vals)+" kg · "+vals.size()+" treeningut");
    ChartView chart=new ChartView(vals);LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,dp(240));cp.setMargins(0,dp(12),0,dp(12));content.addView(chart,cp);
    for(int i=dates.size()-1;i>=0;i--)content.addView(text(dates.get(i)+"    "+vals.get(i)+" kg",14,MUTED,false));
  }
  private class ChartView extends View{
    ArrayList<Float> values;ChartView(ArrayList<Float> a){super(MainActivity.this);values=a;setBackground(round(CARD,18));}
    @Override protected void onDraw(Canvas c){super.onDraw(c);if(values.isEmpty())return;
      Paint p=new Paint(3);float left=dp(20),right=getWidth()-dp(20),top=dp(25),bottom=getHeight()-dp(35);
      p.setColor(LINE);p.setStrokeWidth(dp(1));for(int i=0;i<4;i++){float y=top+(bottom-top)*i/3;c.drawLine(left,y,right,y,p);}
      float max=1;for(float v:values)max=Math.max(v,max);p.setColor(ACCENT);p.setStrokeWidth(dp(3));
      for(int i=0;i<values.size();i++){float x=values.size()==1?(left+right)/2:left+(right-left)*i/(values.size()-1);
        float y=bottom-(values.get(i)/max)*(bottom-top);if(i>0){float xp=left+(right-left)*(i-1)/(values.size()-1);
          float yp=bottom-(values.get(i-1)/max)*(bottom-top);c.drawLine(xp,yp,x,y,p);}c.drawCircle(x,y,dp(4),p);}
    }
  }
  private void progress(){
    title("Minu progress");
    JSONObject data=json("metrics");section("Kehakaal");EditText weight=input(content,"Kehakaal (kg)",data.optString("weight"),true);
    section("Ümbermõõdud (cm)");JSONObject ms=data.optJSONObject("measurements");if(ms==null)ms=new JSONObject();
    LinkedHashMap<String,EditText> sizes=new LinkedHashMap<>();
    for(String name:measurements)sizes.put(name,input(content,name,ms.optString(name),true));
    section("Isiklikud rekordid");JSONObject prs=data.optJSONObject("prs");if(prs==null)prs=new JSONObject();
    LinkedHashMap<String,EditText> lifts=new LinkedHashMap<>();
    for(String name:prNames)lifts.put(name,input(content,name+" (kg või kordused)",prs.optString(name),false));
    EditText other=input(content,"Muud rekordid",data.optString("other"),false);
    btn("Salvesta progress",()->{
      JSONObject updated=new JSONObject(),measure=new JSONObject(),pr=new JSONObject();
      for(String name:sizes.keySet())put(measure,name,value(sizes.get(name)));
      for(String name:lifts.keySet())put(pr,name,value(lifts.get(name)));
      put(updated,"weight",value(weight));put(updated,"measurements",measure);put(updated,"prs",pr);put(updated,"other",value(other));put(updated,"updated",System.currentTimeMillis());
      save("metrics",updated);
      JSONArray log=list("metric_history");log.put(updated);save("metric_history",log);toast("Progress salvestatud");show();
    });
    btn("Vaata kaalu arengut",this::weightProgress);
  }
  private void weightProgress(){
    content.removeAllViews();title("Kehakaalu areng");btn("← Progress",()->go("Progress"));
    JSONArray entries=list("metric_history");ArrayList<Float> vals=new ArrayList<>();ArrayList<String> dates=new ArrayList<>();
    for(int i=0;i<entries.length();i++){JSONObject e=obj(entries,i);double v=decimal(e.optString("weight"));
      if(v>0){vals.add((float)v);dates.add(today(e.optLong("updated",0)));}}
    if(vals.isEmpty()){notice("Andmed puuduvad","Salvesta esmalt kehakaal Progressi vahelehel.");return;}
    ChartView chart=new ChartView(vals);content.addView(chart,new LinearLayout.LayoutParams(-1,dp(240)));
    for(int i=dates.size()-1;i>=0;i--)content.addView(text(dates.get(i)+"    "+vals.get(i)+" kg",14,MUTED,false));
  }
}
