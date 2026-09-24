package UI;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
public class VectorIcon implements Icon {
    public enum Kind {
        HOME,BOOK,CALENDAR,TARGET,CHART,BRIEFCASE,CLOCK,GEAR,PLUS,FILE,USER,STAR
    }
    private final Kind kind;
    private final int s;
    private final Color c;
    public VectorIcon(Kind k,int s,Color c) {
        kind=k;
        this.s=s;
        this.c=c;
    }
    public int getIconWidth() {
        return s;
    }
    public int getIconHeight() {
        return s;
    }
    public void paintIcon(Component comp,Graphics g,int x,int y) {
        Graphics2D q=(Graphics2D)g.create();
        q.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        q.translate(x,y);
        q.setColor(c);
        q.setStroke(new BasicStroke(Math.max(2,s/10f),BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        double z=s;
        switch(kind) {
            case HOME-> {
                q.draw(new Path2D.Double() {
                    {
                        moveTo(z*.12,z*.48);lineTo(z*.5,z*.14);lineTo(z*.88,z*.48);moveTo(z*.23,z*.43);lineTo(z*.23,z*.86);lineTo(z*.77,z*.86);lineTo(z*.77,z*.43);
                    }
                }
                );
            }
            case BOOK-> {
                q.drawRoundRect((int)(z*.1),(int)(z*.18),(int)(z*.34),(int)(z*.66),4,4);
                q.drawRoundRect((int)(z*.56),(int)(z*.18),(int)(z*.34),(int)(z*.66),4,4);
                q.drawLine((int)(z*.5),(int)(z*.22),(int)(z*.5),(int)(z*.84));
            }
            case CALENDAR-> {
                q.drawRoundRect((int)(z*.12),(int)(z*.2),(int)(z*.76),(int)(z*.68),5,5);
                q.drawLine((int)(z*.12),(int)(z*.4),(int)(z*.88),(int)(z*.4));
                q.drawLine((int)(z*.3),(int)(z*.1),(int)(z*.3),(int)(z*.3));
                q.drawLine((int)(z*.7),(int)(z*.1),(int)(z*.7),(int)(z*.3));
            }
            case TARGET-> {
                q.draw(new Ellipse2D.Double(z*.12,z*.12,z*.76,z*.76));
                q.draw(new Ellipse2D.Double(z*.31,z*.31,z*.38,z*.38));
                q.fill(new Ellipse2D.Double(z*.44,z*.44,z*.12,z*.12));
            }
            case CHART-> {
                q.fillRoundRect((int)(z*.15),(int)(z*.55),(int)(z*.16),(int)(z*.3),3,3);
                q.fillRoundRect((int)(z*.42),(int)(z*.36),(int)(z*.16),(int)(z*.49),3,3);
                q.fillRoundRect((int)(z*.69),(int)(z*.18),(int)(z*.16),(int)(z*.67),3,3);
            }
            case BRIEFCASE-> {
                q.drawRoundRect((int)(z*.1),(int)(z*.3),(int)(z*.8),(int)(z*.55),5,5);
                q.drawRoundRect((int)(z*.36),(int)(z*.14),(int)(z*.28),(int)(z*.2),4,4);
                q.drawLine((int)(z*.1),(int)(z*.53),(int)(z*.9),(int)(z*.53));
            }
            case CLOCK-> {
                q.draw(new Ellipse2D.Double(z*.12,z*.12,z*.76,z*.76));
                q.drawLine((int)(z*.5),(int)(z*.5),(int)(z*.5),(int)(z*.28));
                q.drawLine((int)(z*.5),(int)(z*.5),(int)(z*.68),(int)(z*.62));
            }
            case GEAR-> {
                for(int i=0;i<8;i++) {
                    double a=i*Math.PI/4;
                    int cx=(int)(z*.5+Math.cos(a)*z*.32),cy=(int)(z*.5+Math.sin(a)*z*.32);
                    q.drawLine((int)(z*.5+Math.cos(a)*z*.22),(int)(z*.5+Math.sin(a)*z*.22),cx,cy);
                }
                q.draw(new Ellipse2D.Double(z*.25,z*.25,z*.5,z*.5));
                q.fill(new Ellipse2D.Double(z*.43,z*.43,z*.14,z*.14));
            }
            case PLUS-> {
                q.drawLine((int)(z*.5),(int)(z*.15),(int)(z*.5),(int)(z*.85));
                q.drawLine((int)(z*.15),(int)(z*.5),(int)(z*.85),(int)(z*.5));
            }
            case FILE-> {
                q.drawRoundRect((int)(z*.2),(int)(z*.1),(int)(z*.6),(int)(z*.8),4,4);
                q.drawLine((int)(z*.32),(int)(z*.45),(int)(z*.68),(int)(z*.45));
                q.drawLine((int)(z*.32),(int)(z*.62),(int)(z*.68),(int)(z*.62));
            }
            case USER-> {
                q.draw(new Ellipse2D.Double(z*.34,z*.12,z*.32,z*.32));
                q.draw(new Arc2D.Double(z*.18,z*.43,z*.64,z*.5,0,180,Arc2D.OPEN));
            }
            case STAR-> {
                Path2D p=new Path2D.Double();
                for(int i=0;i<10;i++) {
                    double a=-Math.PI/2+i*Math.PI/5,r=i%2==0?z*.4:z*.18,px=z*.5+Math.cos(a)*r,py=z*.5+Math.sin(a)*r;
                    if(i==0)p.moveTo(px,py);
                    else p.lineTo(px,py);
                }
                p.closePath();
                q.draw(p);
            }
        }
        q.dispose();
    }
}
