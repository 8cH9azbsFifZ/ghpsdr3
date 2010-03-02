/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MonitorPanel.java
 *
 * Created on 31-Dec-2009, 17:38:26
 */

package jmonitor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;

/**
 *
 * @author john
 */
public class SpectrumPanel extends javax.swing.JPanel {

    /** Creates new form MonitorPanel */
    public SpectrumPanel() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBackground(new java.awt.Color(0, 0, 0));
        setPreferredSize(new java.awt.Dimension(480, 100));
        addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                formMouseWheelMoved(evt);
            }
        });
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                formMousePressed(evt);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
        });
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                formMouseDragged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 480, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    public void setClient(Client client) {
        this.client=client;
    }

    private void formMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_formMouseWheelMoved
        client.setFrequency(client.getFrequency()+(evt.getWheelRotation()*100));
    }//GEN-LAST:event_formMouseWheelMoved

    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
        if(client.isConnected()) {
            int scrollAmount=(evt.getX() - (WIDTH / 2)) * (client.getSampleRate() / WIDTH);
            switch(evt.getButton()) {
                case MouseEvent.BUTTON1:
                    // Left Button - move to center of filter
                    if(filterHigh<0) {
                        client.setFrequency(client.getFrequency()+(scrollAmount + ((filterHigh - filterLow) / 2)));
                    } else {
                        client.setFrequency(client.getFrequency()+(scrollAmount - ((filterHigh - filterLow) / 2)));
                    }
                    break;
                case MouseEvent.BUTTON3:
                    // Right Button - move to cursor
                    client.setFrequency(client.getFrequency()+scrollAmount);
                    break;
            }
        }
    }//GEN-LAST:event_formMouseClicked

    private void formMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseDragged
        if(client.isConnected()) {
            int increment=startX-evt.getX();
            client.setFrequency(client.getFrequency()+(increment*(client.getSampleRate()/WIDTH)));
            startX=evt.getX();
        }
    }//GEN-LAST:event_formMouseDragged

    private void formMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMousePressed
        if(client.isConnected()) {
            startX=evt.getX();
        }
    }//GEN-LAST:event_formMousePressed


    protected void paintComponent(Graphics g) {
        if(image!=null) {
            g.drawImage(image,0,0,null);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0,0,WIDTH,HEIGHT);
            g.setColor(Color.RED);

            g.drawString(client.getStatus(),50,50);
        }
    }

    public void updateMonitor(float[] samples,int filterLow,int filterHigh,int sampleRate) {
        plotSpectrum(samples,filterLow,filterHigh,sampleRate);
        drawSpectrum();
    }

    private void plotSpectrum(float[] samples,int filterLow,int filterHigh,int sampleRate) {
        this.filterLow=filterLow;
        this.filterHigh=filterHigh;
        for(int i=0;i<WIDTH;i++) {
            X[i]=i;
            Y[i]=(int)Math.floor(((float)spectrumHigh-samples[i])*(float)HEIGHT/(float)(spectrumHigh-spectrumLow));
        }

        filterLeft=(filterLow-(-sampleRate/2))*WIDTH/sampleRate;
        filterRight=(filterHigh-(-sampleRate/2))*WIDTH/sampleRate;
    }

    private void drawSpectrum() {
        if(image==null) image=this.createImage(WIDTH,HEIGHT);
        Graphics graphics=image.getGraphics();
        graphics.setFont(font);
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0,0,WIDTH,HEIGHT);

        // draw the filter
        graphics.setColor(Color.GRAY);
        graphics.fillRect(filterLeft,0,filterRight-filterLeft,HEIGHT);

        // plot frequency markers
        long f=client.getFrequency()-(client.getSampleRate()/2);
        long increment;
        increment=200;

        for(int i=0;i<WIDTH;i++) {
            if((f%20000)<increment) {
                graphics.setColor(Color.YELLOW);
                graphics.drawLine(i,0,i,HEIGHT);
                graphics.setColor(java.awt.Color.GREEN);
                graphics.drawString(frequencyFormat.format((double)f/1000000.0),i-8,15);
            }
            f+=increment;
        }
        // plot horizontal grid
        int V = spectrumHigh - spectrumLow;
        int numSteps = V/20;
        int pixelStepSize = HEIGHT/numSteps;
        for(int i=1; i<numSteps; i++)
        {
            int num = spectrumHigh - i*20;
            int y = (int)Math.floor((spectrumHigh - num)*HEIGHT/V);

            graphics.setColor(Color.YELLOW);
            graphics.drawLine(0, y, WIDTH, y);

            graphics.setColor(Color.GREEN);
            graphics.drawString(Integer.toString(num),3,y+2);
        }

        // draw cursor
        graphics.setColor(Color.RED);
        graphics.drawLine(WIDTH/2, 0, WIDTH/2, HEIGHT);

        // plot the data
        graphics.setColor(Color.WHITE);
        graphics.drawPolyline(X,Y,X.length);

        this.repaint(new Rectangle(WIDTH,HEIGHT));
    }

    private static final int SAMPLES=480;
    private static final int WIDTH=480;
    private static final int HEIGHT=100;

    private Client client;

    private int spectrumHigh=-40;
    private int spectrumLow=-140;

    private int filterLow;
    private int filterHigh;

    private int X[]=new int[WIDTH];
    private int Y[]=new int[WIDTH];
    private int filterLeft;
    private int filterRight;

    private Image image;

    private int startX;

    private Font font=new Font("Ariel",Font.PLAIN,10);

    private DecimalFormat frequencyFormat=new java.text.DecimalFormat("####.00");

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}
