package swingViews;

import javax.swing.JPanel;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.border.LineBorder;

/**
 * A WindowBuilder–friendly 4-3-3 panel using SpringLayout with percent springs.
 */
public class _433SpringPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    // 11 slot panels
    private JPanel goalie;
    private JPanel def1, def2, def3, def4;
    private JPanel mid1, mid2, mid3;
    private JPanel forw1, forw2, forw3;
    
    public _433SpringPanel() {
        initComponents();
    }
    
    private void initComponents() {
        // 1) set SpringLayout
        SpringLayout springLayout = new SpringLayout();
        setLayout(springLayout);
        setOpaque(false); // transparent panel
        
        // 2) create each slot panel, set visuals, add to this
        goalie = new JPanel();
        goalie.setOpaque(false);
        goalie.setBorder(new LineBorder(Color.DARK_GRAY));
        add(goalie);

        def1 = new JPanel();
        def1.setOpaque(false);
        def1.setBorder(new LineBorder(Color.DARK_GRAY));
        add(def1);

        def2 = new JPanel();
        def2.setOpaque(false);
        def2.setBorder(new LineBorder(Color.DARK_GRAY));
        add(def2);

        def3 = new JPanel();
        def3.setOpaque(false);
        def3.setBorder(new LineBorder(Color.DARK_GRAY));
        add(def3);

        def4 = new JPanel();
        def4.setOpaque(false);
        def4.setBorder(new LineBorder(Color.DARK_GRAY));
        add(def4);

        mid1 = new JPanel();
        mid1.setOpaque(false);
        mid1.setBorder(new LineBorder(Color.DARK_GRAY));
        add(mid1);

        mid2 = new JPanel();
        mid2.setOpaque(false);
        mid2.setBorder(new LineBorder(Color.DARK_GRAY));
        add(mid2);

        mid3 = new JPanel();
        mid3.setOpaque(false);
        mid3.setBorder(new LineBorder(Color.DARK_GRAY));
        add(mid3);

        forw1 = new JPanel();
        forw1.setOpaque(false);
        forw1.setBorder(new LineBorder(Color.DARK_GRAY));
        add(forw1);

        forw2 = new JPanel();
        forw2.setOpaque(false);
        forw2.setBorder(new LineBorder(Color.DARK_GRAY));
        add(forw2);

        forw3 = new JPanel();
        forw3.setOpaque(false);
        forw3.setBorder(new LineBorder(Color.DARK_GRAY));
        add(forw3);

        // 3) preferred size for each slot
        Dimension slotDim = new Dimension(60, 60);
        goalie.setPreferredSize(slotDim);
        def1 .setPreferredSize(slotDim);
        def2 .setPreferredSize(slotDim);
        def3 .setPreferredSize(slotDim);
        def4 .setPreferredSize(slotDim);
        mid1 .setPreferredSize(slotDim);
        mid2 .setPreferredSize(slotDim);
        mid3 .setPreferredSize(slotDim);
        forw1.setPreferredSize(slotDim);
        forw2.setPreferredSize(slotDim);
        forw3.setPreferredSize(slotDim);

        // 4) grab parent width/height springs
        Spring parentW = springLayout.getConstraint(SpringLayout.WIDTH, this);
        Spring parentH = springLayout.getConstraint(SpringLayout.HEIGHT, this);

        // 5) inline percent-based constraints:
        //    WEST  = parentW*px - (slotWidth/2)
        //    NORTH = parentH*py - (slotHeight/2)

        // Goalie at (50%, 5%)
        springLayout.putConstraint(
            SpringLayout.WEST, goalie,
            Spring.sum(
                Spring.scale(parentW,  0.50f),
                Spring.scale(springLayout.getConstraints(goalie).getWidth(), -0.5f)
            ),
            SpringLayout.WEST, this
        );
        springLayout.putConstraint(
            SpringLayout.NORTH, goalie,
            Spring.sum(
                Spring.scale(parentH,  0.05f),
                Spring.scale(springLayout.getConstraints(goalie).getHeight(), -0.5f)
            ),
            SpringLayout.NORTH, this
        );

        // Defenders (convex arc)
        float[] defX = { 0.10f, 0.30f, 0.70f, 0.90f };
        float[] defY = { 0.20f, 0.15f, 0.15f, 0.20f };
        JPanel[] defs = { def1, def2, def3, def4 };
        for (int i = 0; i < defs.length; i++) {
            springLayout.putConstraint(
                SpringLayout.WEST, defs[i],
                Spring.sum(
                    Spring.scale(parentW,  defX[i]),
                    Spring.scale(springLayout.getConstraints(defs[i]).getWidth(), -0.5f)
                ),
                SpringLayout.WEST, this
            );
            springLayout.putConstraint(
                SpringLayout.NORTH, defs[i],
                Spring.sum(
                    Spring.scale(parentH,  defY[i]),
                    Spring.scale(springLayout.getConstraints(defs[i]).getHeight(), -0.5f)
                ),
                SpringLayout.NORTH, this
            );
        }

        // Midfield (straight line)
        float[] midX = { 0.25f, 0.50f, 0.75f };
        float   midY = 0.45f;
        JPanel[] mids = { mid1, mid2, mid3 };
        for (int i = 0; i < mids.length; i++) {
            springLayout.putConstraint(
                SpringLayout.WEST, mids[i],
                Spring.sum(
                    Spring.scale(parentW,  midX[i]),
                    Spring.scale(springLayout.getConstraints(mids[i]).getWidth(), -0.5f)
                ),
                SpringLayout.WEST, this
            );
            springLayout.putConstraint(
                SpringLayout.NORTH, mids[i],
                Spring.sum(
                    Spring.scale(parentH,  midY),
                    Spring.scale(springLayout.getConstraints(mids[i]).getHeight(), -0.5f)
                ),
                SpringLayout.NORTH, this
            );
        }

        // Forwards (concave arc)
        float[] fwX = { 0.15f, 0.50f, 0.85f };
        float[] fwY = { 0.75f, 0.80f, 0.75f };
        JPanel[] fws = { forw1, forw2, forw3 };
        for (int i = 0; i < fws.length; i++) {
            springLayout.putConstraint(
                SpringLayout.WEST, fws[i],
                Spring.sum(
                    Spring.scale(parentW,  fwX[i]),
                    Spring.scale(springLayout.getConstraints(fws[i]).getWidth(), -0.5f)
                ),
                SpringLayout.WEST, this
            );
            springLayout.putConstraint(
                SpringLayout.NORTH, fws[i],
                Spring.sum(
                    Spring.scale(parentH,  fwY[i]),
                    Spring.scale(springLayout.getConstraints(fws[i]).getHeight(), -0.5f)
                ),
                SpringLayout.NORTH, this
            );
        }
    }

    // ========== slot getters ==========
    public JPanel getGoalie() { return goalie; }
    public JPanel getDef1()   { return def1;   }
    public JPanel getDef2()   { return def2;   }
    public JPanel getDef3()   { return def3;   }
    public JPanel getDef4()   { return def4;   }
    public JPanel getMid1()   { return mid1;   }
    public JPanel getMid2()   { return mid2;   }
    public JPanel getMid3()   { return mid3;   }
    public JPanel getForw1()  { return forw1;  }
    public JPanel getForw2()  { return forw2;  }
    public JPanel getForw3()  { return forw3;  }

    /** Generic getter by slot index 0..10 */
    public JPanel getSlot(int index) {
        switch (index) {
            case  0: return goalie;
            case  1: return def1;
            case  2: return def2;
            case  3: return def3;
            case  4: return def4;
            case  5: return mid1;
            case  6: return mid2;
            case  7: return mid3;
            case  8: return forw1;
            case  9: return forw2;
            case 10: return forw3;
            default: throw new IllegalArgumentException("Invalid slot " + index);
        }
    }
}
