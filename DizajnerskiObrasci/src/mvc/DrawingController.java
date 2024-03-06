package mvc;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Stack;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import adapter.HexagonAdapter;
import command.CmdModifyPoint;
import command.CmdModifyRectangle;
import command.CmdModifyLine;
import command.CmdAddShape;
import command.CmdBringToBack;
import command.CmdBringToFront;
import command.CmdDeselectShape;
import command.CmdModifyCircle;
import command.CmdModifyDonut;
import command.CmdModifyHexagon;
import command.CmdRemoveShape;
import command.CmdSelectShape;
import command.CmdToBack;
import command.CmdToFront;
import command.Command;
import dialogs.DialogCircle;
import dialogs.DialogDonut;
import dialogs.DialogHexagon;
import dialogs.DialogLine;
import dialogs.DialogPoint;
import dialogs.DialogRectangle;
import geometry.Circle;
import geometry.Donut;
import geometry.Line;
import geometry.Point;
import geometry.Rectangle;
import geometry.Shape;
import observer.BtnObserver;
import observer.UpdateBtnObserver;
import strategy.SaveLog;
import strategy.SaveManager;
import strategy.SavePainting;

public class DrawingController {
	private DrawingModel model;
    private DrawingFrame frame;
    private ArrayList<Shape> selectedShapes=new ArrayList<Shape>();
    private ArrayList<Shape> undoShapes = new ArrayList<Shape>();
    private ArrayList<Shape> redoShapes = new ArrayList<Shape>();
	private Point startPoint;
	private Shape testShape;
	
	private Command command;
	
	private Stack<Command> undoStack = new Stack<Command>();
	private Stack<Command> redoStack = new Stack<Command>();
	
	private int undoCounter = 0;
	private int redoCounter = 0;
	
	private int logCounter = 0;

	

	private ArrayList<String> logList = new ArrayList<String>();
	
	private UpdateBtnObserver btnUpdateObserver;
	
	private BtnObserver btnObserver = new BtnObserver();


    public DrawingController(DrawingModel model, DrawingFrame frame) {
    	
        this.model = model;
        this.frame = frame;
        btnUpdateObserver = new UpdateBtnObserver(frame);
		btnObserver.addPropertyChangeListener(btnUpdateObserver);
        frame.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				thisMouseClicked(e);
			}
		});
       
    }
    
    
    @SuppressWarnings("unused")
	protected void thisMouseClicked(MouseEvent e) {
    	Shape selected;
    	Shape shape;
    	
    	
		if(frame.getTglbtnSelect())
		{
			
			
			selected = null;
			shape = null;
			
			
			Command command = null;
			Point p=new Point(e.getX(),e.getY());
			ListIterator<Shape> it=model.getShapes().listIterator();
			while(it.hasNext())
			{
				shape=it.next();
				if((shape.contains(p)))
				{
					selected=shape;
					
				}
				
			}
			
			
			if(selected != null) 
			{
				if(selected.isSelected())
				{
					command = new CmdDeselectShape(this, selected);
					command.execute();
					
					
					frame.getTextArea().append(command.toString());
					undoStack.push(command);
				} else {
					command = new CmdSelectShape(this, selected);
					command.execute();
					frame.getTextArea().append(command.toString());
					undoStack.push(command);
				}
				undoCounter++;
				
				}
			undoRedoButtons();
			enableDisableButtons();
			redoStack.clear();
			frame.getView().repaint();
			
		} else {
		
		
		
		if(frame.getTglbtnPoint()) {
			Color color = frame.getBtnEdgeColor().getBackground();
			
			DialogPoint dp=new DialogPoint();
			dp.getColorChooser().setColor(color);
			dp.setTbXEdt(false);
			dp.setTxtYEdt(false);
			dp.setTxtX(Integer.toString(e.getX()));
			dp.setTxtY(Integer.toString(e.getY()));
			dp.setVisible(true);
			
			
			Point p=new Point(e.getX(),e.getY());
			
			
			p.setCol(dp.getColor());
			
		
			
			command = new CmdAddShape(model, p);
			command.execute();
			frame.getTextArea().append(command.toString());
			undoCounter++;
			undoStack.push(command);
			redoStack.clear();
			frame.getBtnEdgeColor().setBackground(Color.BLACK);
		}
		else if(frame.getTglbtnLine())
		{
			if(startPoint==null)
			startPoint=new Point(e.getX(),e.getY());
			else {
			Color color = frame.getBtnEdgeColor().getBackground();
			DialogLine dl=new DialogLine();
			dl.getColorChooser().setColor(color);
			dl.setTxtEndCoordXEdt(false);
			dl.setTxtEndCoordYEdt(false);
			dl.setTxtStartCoordXEdt(false);
			dl.setTxtStartCoordYEdt(false);
			dl.setTxtStartCoordX(Integer.toString(startPoint.getX()));
			dl.setTxtStartCoordY(Integer.toString(startPoint.getY()));
			dl.setTxtEndCoordX(Integer.toString(e.getX()));
			dl.setTxtEndCoordY(Integer.toString(e.getY()));
			dl.setVisible(true);
			
			Line l=new Line(startPoint,new Point(e.getX(),e.getY()));
			
			l.setCol(dl.getCol());
			
			command = new CmdAddShape(model, l);
			command.execute();
			frame.getTextArea().append(command.toString());
			undoCounter++;
			undoStack.push(command);
			redoStack.clear();
			startPoint=null;
			frame.getBtnEdgeColor().setBackground(Color.BLACK);

			}
		}
		else if(frame.getTglbtnHexagon())
		{
			Point p=new Point(e.getX(),e.getY());
			
			Color colorEdge = frame.getBtnEdgeColor().getBackground();
			Color colorInner = frame.getBtnInnerColor().getBackground();

			
			DialogHexagon dija=new DialogHexagon();
			dija.getColorChooserEdge().setColor(colorEdge);
			dija.getColorChooserInner().setColor(colorInner);

			dija.setTxtCoordX(Integer.toString(p.getX()));
			dija.setTxtCoordY(Integer.toString(p.getY()));
			dija.setTxtCoordXEdt(false);
			dija.setTxtCoordYEdt(false);
			dija.setVisible(true);
			if(dija.isOk()) {
			try {
			
			int radius=Integer.parseInt(dija.getTextRadius());
			
			HexagonAdapter h= new HexagonAdapter(new Point(Integer.parseInt(dija.getTxtCoordX()), Integer.parseInt(dija.getTxtCoordY()))
					,Integer.parseInt(dija.getTextRadius()),
					false
					);
			h.setHexagonRadius(radius);
			h.setHexagonBorderColor(dija.getColEdge());
			h.setHexagonInnerColor(dija.getColInner());
			command = new CmdAddShape(model, h);
			command.execute();
			frame.getTextArea().append(command.toString());
			
			undoCounter++;
			undoStack.push(command);
			redoStack.clear();
			frame.getBtnEdgeColor().setBackground(Color.BLACK);
			frame.getBtnInnerColor().setBackground(Color.WHITE);
			}
			catch(NumberFormatException ex)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Incorrect data entry.Check that all fields are filled with numeric values!", "Error", JOptionPane.WARNING_MESSAGE);
			}
			catch(Exception ex)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Radius must be positive value!", ex.getMessage(), JOptionPane.WARNING_MESSAGE);
			}
			}
		}
		else if(frame.getTglbtnRectangle())
		{
			
			Point p=new Point(e.getX(),e.getY());
			
			Color colorEdge = frame.getBtnEdgeColor().getBackground();
			Color colorInner = frame.getBtnInnerColor().getBackground();
			
			DialogRectangle dija=new DialogRectangle();
			dija.getColorChooserEdge().setColor(colorEdge);
			dija.getColorChooserInner().setColor(colorInner);
			dija.setTxtXCoordinate(Integer.toString(p.getX()));
			dija.setTxtYCoordinate(Integer.toString(p.getY()));
			dija.setTxtXCoordinateEnabled(false);
			dija.setTxtYCoordinateEnabled(false);
			dija.setVisible(true);
			if(dija.isOk()) {
			try {
			int width=Integer.parseInt(dija.getTxtWidth());
			int height=Integer.parseInt(dija.getTxtHeight());
			Rectangle rct=new Rectangle(p,height,width);
			rct.setEdgeColor(dija.getEdgeColor());
			rct.setInnerColor(dija.getInnerColor());
			command = new CmdAddShape(model, rct);
			command.execute();
			frame.getTextArea().append(command.toString());
			
			undoCounter++;
			undoStack.push(command);
			redoStack.clear();
			frame.getBtnEdgeColor().setBackground(Color.BLACK);
			frame.getBtnInnerColor().setBackground(Color.WHITE);
			}
			catch(NumberFormatException ex)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Incorrect data entry.Check that all fields are filled with numeric values!", "Error", JOptionPane.WARNING_MESSAGE);
			}
			catch(Exception ex)
			{
				JOptionPane.showMessageDialog(new JFrame(), "height and width must be positive values!", "Error", JOptionPane.WARNING_MESSAGE);
			}
			}
		}
		else if(frame.getTglbtnCircle())
		{
			Point center=new Point(e.getX(),e.getY());
			
			Color colorEdge = frame.getBtnEdgeColor().getBackground();
			Color colorInner = frame.getBtnInnerColor().getBackground();
			
			DialogCircle dija=new DialogCircle();
			
			dija.getColorChooserEdge().setColor(colorEdge);
			dija.getColorChooserInner().setColor(colorInner);
			
			dija.setTxtCoordXEdt(false);
			dija.setTxtCoordYEdt(false);
			dija.setTxtCoordX(Integer.toString(center.getX()));
			dija.setTxtCoordY(Integer.toString(center.getY()));
			dija.setVisible(true);
			try
			{
			if(dija.isOk())
			{
				int radius=Integer.parseInt(dija.getTextDiametar());
				Circle circle=new Circle(center,radius);
				
				circle.setColEdge(dija.getColEdge());
				circle.setColInner(dija.getColInner());
				
				command = new CmdAddShape(model, circle);
				command.execute();
				frame.getTextArea().append(command.toString());
				undoCounter++;
				undoStack.push(command);
				redoStack.clear();
				frame.getBtnEdgeColor().setBackground(Color.BLACK);
				frame.getBtnInnerColor().setBackground(Color.WHITE);
			
			}
			}
			catch(NumberFormatException ex)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Incorrect data entry. Check that all fields are filled with numeric values!", "Error", JOptionPane.WARNING_MESSAGE);
			}
			catch(Exception ex)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Diameter value must be positive!", "Error", JOptionPane.WARNING_MESSAGE);	
			}
		}
		else if(frame.getTglbtnCirclewithHole())
		{
			Point center=new Point(e.getX(),e.getY());
			
			Color colorEdge = frame.getBtnEdgeColor().getBackground();
			Color colorInner = frame.getBtnInnerColor().getBackground();
			
			DialogDonut dija=new DialogDonut();
			
			dija.getColorChooserEdge().setColor(colorEdge);
			dija.getColorChooserInner().setColor(colorInner);
			
			dija.setTxtCoordX(Integer.toString(center.getX()));
			dija.setTxtCoordY(Integer.toString(center.getY()));
			dija.setTxtCoordXEditable(false);
			dija.setTxtCoordYEditable(false);	
			dija.setVisible(true);
			try
			{
			if(dija.isOk())
			{
				int innerRadius=Integer.parseInt(dija.getTxtInner());
				int outerRadius=Integer.parseInt(dija.getTxtEdge());
				Donut donut=new Donut(center,outerRadius,innerRadius);
				
				donut.setColEdge(dija.getColEdge());
				donut.setColSmallerEdge(dija.getColEdge());
				donut.setColInner(dija.getColInner());
				
				command = new CmdAddShape(model, donut);
				command.execute();
				frame.getTextArea().append(command.toString());
				undoCounter++;
				undoStack.push(command);
				redoStack.clear();
				frame.getBtnEdgeColor().setBackground(Color.BLACK);
				frame.getBtnInnerColor().setBackground(Color.WHITE);
			}
			}
			catch(NumberFormatException ex)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Incorrect data entry. Check that all fields are filled with numeric values!", "Error", JOptionPane.WARNING_MESSAGE);
			}
			catch(Exception ex)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Diametars must be greater then null and inner diametar of the circle must be less then diametar of the bigger circle!", "Error", JOptionPane.WARNING_MESSAGE);
			}
			
		}
		else if(selectedShapes.get(0)!=null)
		{
			selectedShapes.get(0).setSelected(true);
		}
		else if(selectedShapes!=null) {
			frame.repaint();
		}
		}
		undoRedoButtons();
		enableDisableButtons();
		frame.repaint();
	}
    protected void modify() {
    	if(selectedShapes.get(0)!=null)
		{
			Shape pomShape=selectedShapes.get(0);
	

		if(selectedShapes.get(0) instanceof Point)
		{
			Point oldPoint = (Point) selectedShapes.get(0);

			
			
			
			DialogPoint mt=new DialogPoint();
			mt.setTxtX(Integer.toString(((Point) pomShape).getX()));
			mt.setTxtY(Integer.toString(((Point) pomShape).getY()));
			mt.setColor(((Point)pomShape).getCol());
			mt.setVisible(true);
			try {
			if(mt.isOk())
			{
			Point newPoint = new Point(Integer.parseInt(mt.getTxtX()), Integer.parseInt(mt.getTxtY()), true, mt.getColor());
			
			command = new CmdModifyPoint(oldPoint, newPoint);
			command.execute();
			frame.getTextArea().append(command.toString());
			
			
			undoStack.push(command);
			undoCounter++;
			redoStack.clear();
			
		
			
			}
			
			}
			catch(Exception e)
			{
				System.out.println(e.getMessage().toString());
				JOptionPane.showMessageDialog(new JFrame(), "Incorrect data entry.Check that all fields are filled with numeric values!", "Error!", JOptionPane.WARNING_MESSAGE);
			}
		}
		else if(selectedShapes.get(0) instanceof Line)
		{
			Line oldLine = (Line)selectedShapes.get(0);
			
			
			
			
			DialogLine ml=new DialogLine();
			ml.setTxtStartCoordX(Integer.toString(((Line) pomShape).getStartPoint().getX()));
			ml.setTxtStartCoordY(Integer.toString(((Line) pomShape).getStartPoint().getY()));
			ml.setTxtEndCoordX(Integer.toString(((Line) pomShape).getEndPoint().getX()));
			ml.setTxtEndCoordY(Integer.toString(((Line) pomShape).getEndPoint().getY()));
			ml.setCol(((Line) pomShape).getCol());
			ml.setVisible(true);
			try
			{
			if(ml.isOk())
			{
				Line newLine = new Line(
						new Point(Integer.parseInt(ml.getTxtStartCoordX()),
								Integer.parseInt(ml.getTxtStartCoordY()), true),
						new Point(Integer.parseInt(ml.getTxtEndCoordX()),
								Integer.parseInt(ml.getTxtEndCoordY()), true),
							true,
						ml.getCol()
						
						);
				
				
				command = new CmdModifyLine(oldLine, newLine);
				command.execute();
				
				frame.getTextArea().append(command.toString());
				undoStack.push(command);
				undoCounter++;
				redoStack.clear();
			
			}
			}
			catch(Exception ex)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Incorrect data entry.Check that all fields are filled with numeric values!", "Error", JOptionPane.WARNING_MESSAGE);
			}
		}
		else if(selectedShapes.get(0) instanceof Rectangle)
		{
			Rectangle oldRectangle = (Rectangle) selectedShapes.get(0);
			
			
			DialogRectangle dp=new DialogRectangle();
			dp.setTxtXCoordinate(Integer.toString(((Rectangle)pomShape).getUpperLeftPoint().getX()));
			dp.setTxtYCoordinate(Integer.toString(((Rectangle)pomShape).getUpperLeftPoint().getY()));
			dp.setTxtWidth(Integer.toString(((Rectangle)pomShape).getWidth()));
			dp.setTxtHeight(Integer.toString(((Rectangle)pomShape).getHeight()));
			dp.setInnerColor(((Rectangle)pomShape).getInnerColor());
			dp.setEdgeColor(((Rectangle)pomShape).getEdgeColor());
			dp.setVisible(true);
			try
			{
			if(dp.isOk())
			{
				Rectangle newRectangle = new Rectangle(
						new Point(Integer.parseInt(dp.getTxtXCoordinate()),
								Integer.parseInt(dp.getTxtYCoordinate())),
								Integer.parseInt(dp.getTxtHeight()),
								Integer.parseInt(dp.getTxtWidth()),
								true,
								dp.getEdgeColor(),
								dp.getInnerColor()
						);
				
				command = new CmdModifyRectangle(oldRectangle, newRectangle);
				command.execute();
				frame.getTextArea().append(command.toString());
				
				undoCounter++;
				undoStack.push(command);
				redoStack.clear();
			
			}
			}
			catch(NumberFormatException ex)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Incorrect data entry.Check that all fields are filled with numeric values!", "Error", JOptionPane.WARNING_MESSAGE);
			}
			catch(Exception e)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Height and width must be positive values!", "Error", JOptionPane.WARNING_MESSAGE);
			}
			
		}
		else if(selectedShapes.get(0) instanceof HexagonAdapter)
		{
			HexagonAdapter oldHexagon = (HexagonAdapter) selectedShapes.get(0);
			
			DialogHexagon dh=new DialogHexagon();
			dh.setTxtCoordX(Integer.toString(((HexagonAdapter)pomShape).getHexagonCenter().getX()));
			dh.setTxtCoordY(Integer.toString(((HexagonAdapter)pomShape).getHexagonCenter().getY()));
			dh.setRadius(Integer.toString(((HexagonAdapter)pomShape).getHexagonRadius()));
			dh.setColInner(((HexagonAdapter)pomShape).getHexagonInnerColor());
			dh.setColEdge(((HexagonAdapter)pomShape).getHexagonBorderColor());
			dh.setVisible(true);
			try
			{
			if(dh.isOk())
			{
				HexagonAdapter newHexagon = new HexagonAdapter(
						new Point(Integer.parseInt(dh.getTxtCoordX()),
								Integer.parseInt(dh.getTxtCoordY())),
								Integer.parseInt(dh.getTextRadius()),
								true,
								dh.getColEdge(),
								dh.getColInner()
						);
				
				command = new CmdModifyHexagon(oldHexagon, newHexagon);
				command.execute();
				frame.getTextArea().append(command.toString());
				
				undoCounter++;
				undoStack.push(command);
				redoStack.clear();

			}
			}
			catch(NumberFormatException ex)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Incorrect data entry.Check that all fields are filled with numeric values!", "Error", JOptionPane.WARNING_MESSAGE);
			}
			catch(Exception e)
			{
				System.out.println("this" + e.getMessage());

				JOptionPane.showMessageDialog(new JFrame(), "Value of radius must be positive!", "Error", JOptionPane.WARNING_MESSAGE);
			}
			
		}
		else if(selectedShapes.get(0) instanceof Donut)
		{
			Donut oldDonut = (Donut) selectedShapes.get(0);
			
			DialogDonut dk=new DialogDonut();
			dk.setTxtCoordX(Integer.toString(((Donut)pomShape).getCenter().getX()));
			dk.setTxtCoordY(Integer.toString(((Donut)pomShape).getCenter().getY()));
			dk.setTxtInner(Integer.toString(((Donut)pomShape).getInnerRadius()));
			dk.setTxtEdge(Integer.toString(((Donut)pomShape).getRadius()));
			dk.setColEdge((((Donut)pomShape).getColEdge()));
			dk.setColInner((((Donut)pomShape).getColInner()));
			dk.setVisible(true);
			try {
			if(dk.isOk())
			{
				Donut newDonut = new Donut(
						new Point(Integer.parseInt(dk.getTxtCoordX()),
								Integer.parseInt(dk.getTxtCoordY())),
								Integer.parseInt(dk.getTxtEdge()),
								Integer.parseInt(dk.getTxtInner()),
								true,
								dk.getColEdge(),
								dk.getColInner()
						);
				command = new CmdModifyDonut(oldDonut, newDonut);
				command.execute();
				frame.getTextArea().append(command.toString());
				
				undoCounter++;
				undoStack.push(command);
				redoStack.clear();
				

			}
			}
			catch(NumberFormatException e)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Incorrect data entry.Check that all fields are filled with numeric values!", "Error", JOptionPane.WARNING_MESSAGE);
			}
			catch(Exception ex)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Diametars must be greater then null and inner diametar of the circle must be less then diametar of the bigger circle!", "Error", JOptionPane.WARNING_MESSAGE);
			}
			
		}else if(selectedShapes.get(0) instanceof Circle)
		{
			Circle oldCircle = (Circle) selectedShapes.get(0);
			
			DialogCircle dk=new DialogCircle();
			dk.setTxtCoordX(Integer.toString(((Circle)pomShape).getCenter().getX()));
			dk.setTxtCoordY(Integer.toString(((Circle)pomShape).getCenter().getY()));
			dk.setDiametar(Integer.toString(((Circle)pomShape).getRadius()));
			dk.setColInner(((Circle)pomShape).getColInner());
			dk.setColEdge(((Circle)pomShape).getColEdge());
			dk.setVisible(true);
			try
			{
			if(dk.isOk())
			{
				Circle newCircle = new Circle(
						new Point(Integer.parseInt(dk.getTxtCoordX()),
								Integer.parseInt(dk.getTxtCoordY())),
						Integer.parseInt(dk.getTextDiametar()),
						true,
						dk.getColEdge(),
						dk.getColInner()
						);
				
				command = new CmdModifyCircle(oldCircle, newCircle);
				command.execute();
				frame.getTextArea().append(command.toString());
				
				undoCounter++;
				undoStack.push(command);
				redoStack.clear();
		
			}
			}
			catch(NumberFormatException ex)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Incorrect data entry.Check that all fields are filled with numeric values!", "Error", JOptionPane.WARNING_MESSAGE);
			}
			catch(Exception e)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Value of diameter must be positive!", "Error", JOptionPane.WARNING_MESSAGE);
			}
		}
		
		}
		else
		{
			setStartPoint(null);
			JOptionPane.showMessageDialog(new JFrame(), "No shape selected!", "Error!", JOptionPane.WARNING_MESSAGE);
		}
    	
		enableDisableButtons();
		//frame.getView().repaint();
	}
    
    protected void delete() { 
    	Shape shape;
    	
    	for(int i = selectedShapes.size() - 1; i>=0;i--)
    	{
    		shape = selectedShapes.get(0);
    		command = new CmdRemoveShape(model, shape, model.getShapes().indexOf(shape));
    		command.execute();
    		frame.getTextArea().append(command.toString());
    		selectedShapes.remove(shape);
    		undoShapes.add(shape);
    		undoStack.push(command);
    		
    		
    		undoCounter++;
    		
    		
    	}
    	
    	if(JOptionPane.showConfirmDialog(new JFrame(), "Are you sure you want to delete selected shape?","Check",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
		{
    		redoStack.clear();
        	undoRedoButtons();
        	frame.repaint();
		}
		enableDisableButtons();

    	
    

	}
  
    
    public ArrayList<Shape> getSelectedShapes() {
		return selectedShapes;
	}

	
	public void setStartPoint(Point p)
	{
		this.startPoint=p;
	}
	public void saveLog() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Save Log");
		
		
		FileNameExtensionFilter fileNameExtensionFilter = new FileNameExtensionFilter(".txt", "txt");
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setFileFilter(fileNameExtensionFilter);

		if (fileChooser.showSaveDialog(frame.getParent()) == JFileChooser.APPROVE_OPTION) {
			
			
			File file = fileChooser.getSelectedFile();
			
			String filePath = file.getAbsolutePath();
			
			File log = new File(filePath + ".txt");

			SaveManager manager = new SaveManager(new SaveLog());
			manager.save(frame, log);
			
			System.out.println(fileChooser.getSelectedFile().getName() + "log successfully saved " + " file!");
		}
		frame.getView().repaint();
	}
	
	
	
	public void savePainting() throws IOException, NotSerializableException {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Save Painting");
		
		fileChooser.setAcceptAllFileFilterUsed(false);
		
		FileNameExtensionFilter filter = new FileNameExtensionFilter(".bin", "bin");
		fileChooser.setFileFilter(filter);

		int selection = fileChooser.showSaveDialog(null);

		if (selection == JFileChooser.APPROVE_OPTION) {
			File log;
			
			File painting = fileChooser.getSelectedFile();
			
			
			
			String filePath = painting.getAbsolutePath();
			if (!filePath.contains(".") && !filePath.endsWith(".bin")) {
				log = new File(filePath + ".txt");
				
				painting = new File(filePath + ".bin");
				
			}

			String fileName = painting.getPath();
			
			
			if (fileName.substring(fileName.lastIndexOf("."), fileName.length()).contains(".bin")) {
				fileName = painting.getAbsolutePath().substring(0, fileName.lastIndexOf(".")) + ".txt";
				
				SaveManager savePainting = new SaveManager(new SavePainting());
				SaveManager saveLog = new SaveManager(new SaveLog());
				
				log = new File(fileName);
				
				savePainting.save(model, painting);
				saveLog.save(frame, log);
				
				System.out.println("Painting saved, location: " + painting.getAbsolutePath());
				
			} else {
				JOptionPane.showMessageDialog(null, "Wrong file extension!");
			}
		}
	}
	
	public void openPainting() throws IOException, ClassNotFoundException {
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(false);
		
		FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter(".bin", "bin");
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setFileFilter(extensionFilter);

		fileChooser.setDialogTitle("Open Painting");
		int userSelection = fileChooser.showOpenDialog(null);

		if (userSelection == JFileChooser.APPROVE_OPTION) {
			
			File loadingPainting = fileChooser.getSelectedFile();
			
			loadPainting(loadingPainting);

		}
	}
	
	public void loadPainting(File paintingToLoad) throws FileNotFoundException, IOException, ClassNotFoundException {
		frame.getTextArea().setText("");

		File file = new File(paintingToLoad.getAbsolutePath().replace("bin", "txt"));

		if (file.length() == 0) {
			System.out.println("\"" + paintingToLoad.getName() + "\" file is empty!");
			return;
		}

		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
		String logLine;

		while ((logLine = bufferedReader.readLine()) != null) {
			frame.getTextArea().append(logLine + "\n");
		}
		bufferedReader.close();

		ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(paintingToLoad));
		try {

			model.getShapes().addAll((ArrayList<Shape>) objectInputStream.readObject());
			objectInputStream.close();

		} catch (InvalidClassException ice) {
			ice.printStackTrace();
		} catch (SocketTimeoutException ste) {
			ste.printStackTrace();
		} catch (EOFException eofe) {
			eofe.printStackTrace();
		} catch (IOException exc) {
			exc.printStackTrace();
		}
		frame.getView().repaint();
	}
	
	public void openLog() throws IOException {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Open log");
		FileNameExtensionFilter fileNameExtensionFilter = new FileNameExtensionFilter(".txt", "txt");
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setFileFilter(fileNameExtensionFilter);

		int userSelection = fileChooser.showOpenDialog(null);

		if (userSelection == JFileChooser.APPROVE_OPTION) {
			File logToLoad = fileChooser.getSelectedFile();
			loadLog(logToLoad);
		}
	}
	
	
	public void loadLog(File logToLoad) throws IOException {
		try {
			frame.getTextArea().setText("");
			if (logToLoad.length() == 0) {
				System.out.println("\"" + logToLoad.getName() + "\" file is empty");
				return;
			}
			BufferedReader br = new BufferedReader(new FileReader(logToLoad));
			String stringLine;
			
			
			while ((stringLine = br.readLine()) != null) {
				logList.add(stringLine);
			}
			br.close();
			frame.getTglBtnPoint().setEnabled(false);
			frame.getTglBtnLine().setEnabled(false);
			frame.getTglBtnDonut().setEnabled(false);
			frame.getTglBtnCircle().setEnabled(false);
			frame.getTglBtnHexagon().setEnabled(false);
			frame.getBtnUndo().setEnabled(false);
			frame.getTglBtnRectangle().setEnabled(false);
			frame.getBtnLoadNext().setEnabled(true);

		} catch (Exception e) {
			System.err.println("There has been an error: " + e.getMessage());
		}
	}
	
	
	
	
	
	public void loadNext () throws Exception {
		Shape shape = null;
		
		if (logCounter < logList.size()) {
			String row = logList.get(logCounter);
			
			if (row.contains("Point")) {
				int x = Integer.parseInt(row.substring(findIndexOf(1, '(', row) + 1, findIndexOf(1, ',', row)));
				int y = Integer.parseInt(row.substring(findIndexOf(1, ',', row) + 2, findIndexOf(1, ')', row)));
				int edgeColor = Integer.parseInt(row.substring(findIndexOf(2, '(', row) + 1, findIndexOf(2, ')', row)));
				shape = new Point(x, y, new Color(edgeColor));
			} else if (row.contains("Line")) {
				int startPointX = Integer.parseInt(row.substring(findIndexOf(1, '(', row) + 1, findIndexOf(1,',', row)));
				int startPointY = Integer.parseInt(row.substring(findIndexOf(1, ',', row) + 2, findIndexOf(1, ')', row)));
				int endPointX = Integer.parseInt(row.substring(findIndexOf(2, '(', row) + 1, findIndexOf(2, ',', row)));
				int endPointY = Integer.parseInt(row.substring(findIndexOf(2, ',', row) + 2, findIndexOf(2, ')', row)));
				int color = Integer.parseInt(row.substring(findIndexOf(3, '(', row) + 1, findIndexOf(3, ')', row)));
				shape = new Line(new Point(startPointX, startPointY), new Point(endPointX, endPointY), new Color(color));
			} else if (row.contains("Rectangle")) {
				int upperLeftPointX = Integer.parseInt(row.substring(findIndexOf(1, '(', row) + 1, findIndexOf(1, ',', row)));
				int upperLeftPointY = Integer.parseInt(row.substring(findIndexOf(1, ',', row) + 2, findIndexOf(1, ')', row)));
				int height = Integer.parseInt(row.substring(findIndexOf(2, '=', row) + 1, findIndexOf(3, ',', row)));
				int width = Integer.parseInt(row.substring(findIndexOf(3, '=', row) + 1, findIndexOf(4, ',', row)));
				int innerColor = Integer.parseInt(row.substring(findIndexOf(2, '(', row) + 1, findIndexOf(2, ')', row)));
				int edgeColor = Integer.parseInt(row.substring(findIndexOf(3, '(', row) + 1, findIndexOf(3, ')', row)));
				shape = new Rectangle(new Point(upperLeftPointX, upperLeftPointY), height, width, new Color(edgeColor), new Color(innerColor));
			} else if (row.contains("Circle")) {
				int centerX = Integer.parseInt(row.substring(findIndexOf(1, '(', row) + 1, findIndexOf(1, ',', row)));
				int centerY = Integer.parseInt(row.substring(findIndexOf(1, ',', row) + 2, findIndexOf(1, ')', row)));
				int radius =  Integer.parseInt(row.substring(findIndexOf(2, '=', row) + 1, findIndexOf(3, ',', row)));
				int innerColor = Integer.parseInt(row.substring(findIndexOf(2, '(', row) + 1, findIndexOf(2, ')', row)));
				int edgeColor = Integer.parseInt(row.substring(findIndexOf(3, '(', row) + 1, findIndexOf(3, ')', row)));
				shape = new Circle(new Point(centerX, centerY), radius, new Color(edgeColor), new Color(innerColor));	
			} else if (row.contains("Donut")) {
				int centerX = Integer.parseInt(row.substring(findIndexOf(1, '(', row) + 1, findIndexOf(1, ',', row)));
				int centerY = Integer.parseInt(row.substring(findIndexOf(1, ',', row) + 2, findIndexOf(1, ')', row)));
				int radius = Integer.parseInt(row.substring(findIndexOf(1, '=', row) + 2, findIndexOf(3, ',', row)));
				int innerRadius = Integer.parseInt(row.substring(findIndexOf(2, '=', row) + 1, findIndexOf(4, ',', row)));
				int innerColor = Integer.parseInt(row.substring(findIndexOf(2, '(', row) + 1, findIndexOf(2, ')', row)));
				int edgeColor = Integer.parseInt(row.substring(findIndexOf(3, '(', row) + 1, findIndexOf(3, ')', row)));
				shape = new Donut(new Point(centerX, centerY), radius, innerRadius, new Color(edgeColor), new Color(innerColor));
			} else if (row.contains("Hexagon")) {
				int centerX = Integer.parseInt(row.substring(findIndexOf(1, '(', row) + 1, findIndexOf(1, ',', row)));
				int centerY = Integer.parseInt(row.substring(findIndexOf(1, ',', row) + 2, findIndexOf(1, ')', row)));
				int radius = Integer.parseInt(row.substring(findIndexOf(1, '=', row) + 1, findIndexOf(3, ',', row)));
				int color = Integer.parseInt(row.substring(findIndexOf(2, '(', row) + 1, findIndexOf(2, ')', row)));
				int innerColor = Integer.parseInt(row.substring(findIndexOf(3, '(', row) + 1, findIndexOf(3, ')', row)));
				shape = new HexagonAdapter(new Point(centerX, centerY), radius, new Color(color), new Color(innerColor));
			}
			
			if (row.contains("Added")) {
				CmdAddShape cmdAddShape;
				
				if(row.contains("Undo")) {
					cmdAddShape = (CmdAddShape) undoStack.peek();
					cmdAddShape.unexecute();
					undoStack.pop();
					redoStack.push(cmdAddShape);
					frame.getTextArea().append("Undo " + cmdAddShape.toString());
				} else if (row.contains("Redo")) {
					cmdAddShape = (CmdAddShape) redoStack.peek();
					cmdAddShape.execute();
					redoStack.pop();
					undoStack.push(cmdAddShape);
					frame.getTextArea().append("Redo " + cmdAddShape.toString());
				} else {
					cmdAddShape = new CmdAddShape(model, shape);
					cmdAddShape.execute();
					undoStack.push(cmdAddShape);
					redoStack.clear();
					frame.getTextArea().append(cmdAddShape.toString());
				}
			}
			if (row.contains("Selected")) {
				CmdSelectShape cmdSelectShape;
				
				if(row.contains("Undo")) {
					cmdSelectShape = (CmdSelectShape) undoStack.peek();
					cmdSelectShape.unexecute();
					undoStack.pop();
					redoStack.push(cmdSelectShape);
					frame.getTextArea().append("Undo " + cmdSelectShape.toString());
				} else if (row.contains("Redo")){
					cmdSelectShape = (CmdSelectShape) redoStack.peek();
					cmdSelectShape.execute();
					redoStack.pop();
					undoStack.push(cmdSelectShape);
					frame.getTextArea().append("Redo " + cmdSelectShape.toString());
				} else {
					if(row.contains("Hexagon")) {
						shape = model.getShapes().get(model.getShapes().indexOf(shape));
					}else {
						shape = model.getShapes().get(model.getShapes().indexOf(shape) + 1);
					}
					
					
					cmdSelectShape = new CmdSelectShape(this, shape);
					cmdSelectShape.execute();
					undoStack.push(cmdSelectShape);
					redoStack.clear();
					frame.getTextArea().append(cmdSelectShape.toString());
				} 
			} else if (row.contains("Deselected")) {
				CmdDeselectShape cmdDeselectShape;
				
				if(row.contains("Undo")) {
					cmdDeselectShape = (CmdDeselectShape) undoStack.peek();
					cmdDeselectShape.unexecute();
					undoStack.pop();
					redoStack.push(cmdDeselectShape);
					frame.getTextArea().append("Undo " + cmdDeselectShape.toString());
				} else if (row.contains("Redo")) {
					cmdDeselectShape = (CmdDeselectShape) redoStack.peek();
					cmdDeselectShape.execute();
					redoStack.pop();
					undoStack.push(cmdDeselectShape);
					frame.getTextArea().append("Redo " + cmdDeselectShape.toString());
				} else {
					shape = selectedShapes.get(selectedShapes.indexOf(shape));
					cmdDeselectShape = new CmdDeselectShape(this, shape);
					cmdDeselectShape.execute();
					undoStack.push(cmdDeselectShape); 
					redoStack.clear();
					frame.getTextArea().append(cmdDeselectShape.toString());
				}
			}else if (row.contains("Modified")) {
				if (shape instanceof Point) {
					CmdModifyPoint cmdModifyPoint;
					
					if(row.contains("Undo")) {
						cmdModifyPoint = (CmdModifyPoint) undoStack.peek();
						cmdModifyPoint.unexecute();
						undoStack.pop();
						redoStack.push(cmdModifyPoint);
						frame.getTextArea().append("Undo " + cmdModifyPoint.toString());
					} else if (row.contains("Redo")) {
						cmdModifyPoint = (CmdModifyPoint) redoStack.peek();
						cmdModifyPoint.execute(); 
						redoStack.pop();
						undoStack.push(cmdModifyPoint);
						frame.getTextArea().append("Redo " + cmdModifyPoint.toString());
					} else {
						shape = selectedShapes.get(0);
						
						Point newPoint = new Point();
						
						newPoint.setX(Integer.parseInt(row.substring(findIndexOf(3, '(', row) + 1, findIndexOf(2, ',', row))));
						newPoint.setY(Integer.parseInt(row.substring(findIndexOf(2, ',', row) + 2, findIndexOf(3, ')', row))));
						newPoint.setCol(new Color(Integer.parseInt(row.substring(findIndexOf(4, '(', row) + 1, findIndexOf(4, ')', row)))));

						cmdModifyPoint = new CmdModifyPoint((Point) shape, newPoint);
						cmdModifyPoint.execute();
						undoStack.push(cmdModifyPoint);
						redoStack.clear();
						frame.getTextArea().append(cmdModifyPoint.toString());
					}
				} else if (shape instanceof Line) {
					CmdModifyLine cmdModifyLine;
					
					if(row.contains("Undo")) {
						cmdModifyLine = (CmdModifyLine) undoStack.peek();
						cmdModifyLine.unexecute();
						undoStack.pop();
						redoStack.push(cmdModifyLine);
						frame.getTextArea().append("Undo " + cmdModifyLine.toString());
					} else if (row.contains("Redo")) {
						cmdModifyLine = (CmdModifyLine) redoStack.peek();
						cmdModifyLine.execute(); 
						redoStack.pop();
						undoStack.push(cmdModifyLine);
						frame.getTextArea().append("Redo " + cmdModifyLine.toString());
					} else {
						shape = selectedShapes.get(0);
						Point newStartPoint = new Point();
						Point newEndPoint = new Point();
						
						newStartPoint.setX(Integer.parseInt(row.substring(findIndexOf(4, '(', row) + 1, findIndexOf(3, ',', row))));
						newStartPoint.setY(Integer.parseInt(row.substring(findIndexOf(3, ',', row) + 2, findIndexOf(4, ')', row))));
						newEndPoint.setX(Integer.parseInt(row.substring(findIndexOf(5, '(', row) + 1, findIndexOf(4, ',', row))));
						newEndPoint.setY(Integer.parseInt(row.substring(findIndexOf(4, ',', row) + 2, findIndexOf(5, ')', row))));
						
						Line newLine = new Line(newStartPoint, newEndPoint);
						newLine.setCol(new Color(Integer.parseInt(row.substring(findIndexOf(6, '(', row) + 1, findIndexOf(6, ')', row)))));

						cmdModifyLine = new CmdModifyLine((Line) shape, newLine);
						cmdModifyLine.execute();
						undoStack.push(cmdModifyLine);
						redoStack.clear();
						frame.getTextArea().append(cmdModifyLine.toString());
					}
				} else if (shape instanceof HexagonAdapter) {
					CmdModifyHexagon cmdModifyHexagon;
					
					if(row.contains("Undo")) {
						cmdModifyHexagon = (CmdModifyHexagon) undoStack.peek();
						cmdModifyHexagon.unexecute(); 
						undoStack.pop();
						redoStack.push(cmdModifyHexagon);
						frame.getTextArea().append("Undo " + cmdModifyHexagon.toString());
					} else if (row.contains("Redo")) {
						cmdModifyHexagon = (CmdModifyHexagon) redoStack.peek();
						cmdModifyHexagon.execute(); 
						redoStack.pop();
						undoStack.push(cmdModifyHexagon);
						frame.getTextArea().append("Redo " + cmdModifyHexagon.toString());
					} else {
						shape = selectedShapes.get(0);
						Point center = new Point();
						int radius, edgeColor, innerColor;
						
						center.setX(Integer.parseInt(row.substring(findIndexOf(4, '(', row) + 1, findIndexOf(5, ',', row))));
						center.setY(Integer.parseInt(row.substring(findIndexOf(5, ',', row) + 2, findIndexOf(4, ')', row))));
						radius = (Integer.parseInt(row.substring(findIndexOf(2, '=', row) + 1, findIndexOf(7, ',', row))));
						edgeColor = (Integer.parseInt(row.substring(findIndexOf(5, '(', row) + 1, findIndexOf(5, ')', row))));
						innerColor = (Integer.parseInt(row.substring(findIndexOf(6, '(', row) + 1, findIndexOf(6, ')', row))));
						
						HexagonAdapter newHexagon = new HexagonAdapter(center, radius, new Color(edgeColor), new Color(innerColor));
						
						cmdModifyHexagon = new CmdModifyHexagon((HexagonAdapter) shape, newHexagon);
						cmdModifyHexagon.execute();
						undoStack.push(cmdModifyHexagon);
						redoStack.clear();
						frame.getTextArea().append(cmdModifyHexagon.toString());
					}
				} else if (shape instanceof Rectangle) {
					CmdModifyRectangle cmdModifyRectangle;
					
					if (row.contains("Undo")) {
						cmdModifyRectangle = (CmdModifyRectangle) undoStack.peek();
						cmdModifyRectangle.unexecute();
						undoStack.pop();
						redoStack.push(cmdModifyRectangle);
						frame.getTextArea().append(cmdModifyRectangle.toString());
					} else if (row.contains("Redo")) {
						cmdModifyRectangle = (CmdModifyRectangle) redoStack.peek();
						cmdModifyRectangle.execute(); 
						redoStack.pop();
						undoStack.push(cmdModifyRectangle);
						frame.getTextArea().append("Redo " + cmdModifyRectangle.toString());
					} else {
						shape = selectedShapes.get(0);
						Point upperLeftPoint = new Point();
						int width, height, edgeColor, innerColor;
						
						upperLeftPoint.setX(Integer.parseInt(row.substring(findIndexOf(4, '(', row) + 1, findIndexOf(6, ',', row))));
						upperLeftPoint.setY(Integer.parseInt(row.substring(findIndexOf(6, ',', row) + 2, findIndexOf(4, ')', row))));
						
						width = Integer.parseInt(row.substring(findIndexOf(8, '=', row) + 1, findIndexOf(9, ',', row)));
						height = Integer.parseInt(row.substring(findIndexOf(7, '=', row) + 1, findIndexOf(8, ',', row)));
						
						edgeColor = Integer.parseInt(row.substring(findIndexOf(6, '(', row) + 1, findIndexOf(6, ')', row)));
						innerColor = Integer.parseInt(row.substring(findIndexOf(5, '(', row) + 1, findIndexOf(5, ')', row)));						
					
						Rectangle newRectangle = new Rectangle(upperLeftPoint, height, width, new Color(edgeColor), new Color(innerColor));
						
						cmdModifyRectangle = new CmdModifyRectangle((Rectangle) shape, newRectangle);
						cmdModifyRectangle.execute(); 
						undoStack.push(cmdModifyRectangle);
						redoStack.clear();
						frame.getTextArea().append(cmdModifyRectangle.toString());
					}
				} else if (shape instanceof Donut) {
					CmdModifyDonut cmdModifyDonut;
					
					if(row.contains("Undo")) {
						cmdModifyDonut = (CmdModifyDonut) undoStack.peek();
						cmdModifyDonut.unexecute(); 
						undoStack.pop();
						redoStack.push(cmdModifyDonut);
						frame.getTextArea().append("Undo " + cmdModifyDonut.toString());
					} else if (row.contains("Redo")) {
						cmdModifyDonut = (CmdModifyDonut) redoStack.peek();
						cmdModifyDonut.execute(); 
						redoStack.pop();
						undoStack.push(cmdModifyDonut);
						frame.getTextArea().append("Redo " + cmdModifyDonut.toString());
					} else {
						shape = selectedShapes.get(0);
						Point center = new Point();
						int radius, innerRadius, edgeColor, innerColor;
						
						center.setX(Integer.parseInt(row.substring(findIndexOf(4, '(', row) + 1, findIndexOf(6, ',', row))));
						center.setY(Integer.parseInt(row.substring(findIndexOf(6, ',', row) + 2, findIndexOf(4, ')', row))));
						
						radius = Integer.parseInt(row.substring(findIndexOf(5, '=', row) + 2, findIndexOf(8, ',', row)));
						innerRadius = Integer.parseInt(row.substring(findIndexOf(6, '=', row) + 1, findIndexOf(9, ',', row)));
						
						edgeColor = Integer.parseInt(row.substring(findIndexOf(6, '(', row) + 1, findIndexOf(6, ')', row)));
						innerColor = Integer.parseInt(row.substring(findIndexOf(5, '(', row) + 1, findIndexOf(5, ')', row)));
						
						Donut newDonut = new Donut(center, radius, innerRadius, new Color(edgeColor), new Color(innerColor));
						
						cmdModifyDonut = new CmdModifyDonut((Donut) shape, newDonut);
						cmdModifyDonut.execute(); 
						undoStack.push(cmdModifyDonut);
						redoStack.clear();
						frame.getTextArea().append(cmdModifyDonut.toString());	
					}
				} else if (shape instanceof Circle) {
					CmdModifyCircle cmdModifyCircle;
					
					if(row.contains("Undo")) {
						cmdModifyCircle = (CmdModifyCircle) undoStack.peek();
						cmdModifyCircle.unexecute(); 
						undoStack.pop();
						redoStack.push(cmdModifyCircle);
						frame.getTextArea().append("Undo " + cmdModifyCircle.toString());
					} else if (row.contains("Redo")) {
						cmdModifyCircle = (CmdModifyCircle) redoStack.peek();
						cmdModifyCircle.execute(); 
						redoStack.pop();
						undoStack.push(cmdModifyCircle);
						frame.getTextArea().append("Redo " + cmdModifyCircle.toString());
					} else {
						shape = selectedShapes.get(0);
						Point center = new Point();
						int radius, edgeColor, innerColor;
						
						center.setX(Integer.parseInt(row.substring(findIndexOf(4, '(', row) + 1, findIndexOf(5, ',', row))));
						center.setY(Integer.parseInt(row.substring(findIndexOf(5, ',', row) + 2, findIndexOf(4, ')', row))));
						radius = Integer.parseInt(row.substring(findIndexOf(6, '=', row) + 1, findIndexOf(7, ',', row)));
						edgeColor = Integer.parseInt(row.substring(findIndexOf(6, '(', row) + 1, findIndexOf(6, ')', row)));
						innerColor = Integer.parseInt(row.substring(findIndexOf(5, '(', row) + 1, findIndexOf(5, ')', row)));
						
						Circle newCircle = new Circle(center, radius, new Color(edgeColor), new Color(innerColor));
						
						cmdModifyCircle = new CmdModifyCircle((Circle) shape, newCircle);
						cmdModifyCircle.execute(); 
						
						undoStack.push(cmdModifyCircle);
						redoStack.clear();
						frame.getTextArea().append(cmdModifyCircle.toString());
					}
				}
			} else if (row.contains("Modified")) {
				if (shape instanceof Point) {
					CmdModifyPoint cmdModifyPoint;
					
					if(row.contains("Undo")) {
						cmdModifyPoint = (CmdModifyPoint) undoStack.peek();
						cmdModifyPoint.unexecute();
						undoStack.pop();
						redoStack.push(cmdModifyPoint);
						frame.getTextArea().append("Undo " + cmdModifyPoint.toString());
					} else if (row.contains("Redo")) {
						cmdModifyPoint = (CmdModifyPoint) redoStack.peek();
						cmdModifyPoint.execute(); 
						redoStack.pop();
						undoStack.push(cmdModifyPoint);
						frame.getTextArea().append("Redo " + cmdModifyPoint.toString());
					} else {
						shape = selectedShapes.get(0);
						Point newPoint = new Point();
						
						newPoint.setX(Integer.parseInt(row.substring(findIndexOf(3, '(', row) + 1, findIndexOf(3, ',', row))));
						newPoint.setY(Integer.parseInt(row.substring(findIndexOf(3, ',', row) + 2, findIndexOf(3, ')', row))));
						newPoint.setCol(new Color(Integer.parseInt(row.substring(findIndexOf(4, '(', row) + 1, findIndexOf(4, ')', row)))));

						cmdModifyPoint = new CmdModifyPoint((Point) shape, newPoint);
						cmdModifyPoint.execute();
						undoStack.push(cmdModifyPoint);
						redoStack.clear();
						frame.getTextArea().append(cmdModifyPoint.toString());
					}
				} else if (shape instanceof Line) {
					CmdModifyLine cmdModifyLine;
					
					if(row.contains("Undo")) {
						cmdModifyLine = (CmdModifyLine) undoStack.peek();
						cmdModifyLine.unexecute();
						undoStack.pop();
						redoStack.push(cmdModifyLine);
						frame.getTextArea().append("Undo " + cmdModifyLine.toString());
					} else if (row.contains("Redo")) {
						cmdModifyLine = (CmdModifyLine) redoStack.peek();
						cmdModifyLine.execute(); 
						redoStack.pop();
						undoStack.push(cmdModifyLine);
						frame.getTextArea().append("Redo " + cmdModifyLine.toString());
					} else {
						shape = selectedShapes.get(0);
						Point newStartPoint = new Point();
						Point newEndPoint = new Point();
						
						newStartPoint.setX(Integer.parseInt(row.substring(findIndexOf(4, '(', row) + 1, findIndexOf(4, ',', row))));
						newStartPoint.setY(Integer.parseInt(row.substring(findIndexOf(4, ',', row) + 2, findIndexOf(4, ')', row))));
						newEndPoint.setX(Integer.parseInt(row.substring(findIndexOf(5, '(', row) + 1, findIndexOf(5, ',', row))));
						newEndPoint.setY(Integer.parseInt(row.substring(findIndexOf(5, ',', row) + 2, findIndexOf(5, ')', row))));
						
						Line newLine = new Line(newStartPoint, newEndPoint);
						newLine.setCol(new Color(Integer.parseInt(row.substring(findIndexOf(6, '(', row) + 1, findIndexOf(6, ')', row)))));

						cmdModifyLine = new CmdModifyLine((Line) shape, newLine);
						cmdModifyLine.execute();
						undoStack.push(cmdModifyLine);
						redoStack.clear();
						frame.getTextArea().append(cmdModifyLine.toString());
					}
				} else if (shape instanceof HexagonAdapter) {
					CmdModifyHexagon cmdModifyHexagon;
					
					if(row.contains("Undo")) {
						cmdModifyHexagon = (CmdModifyHexagon) undoStack.peek();
						cmdModifyHexagon.unexecute(); 
						undoStack.pop();
						redoStack.push(cmdModifyHexagon);
						frame.getTextArea().append("Undo " + cmdModifyHexagon.toString());
					} else if (row.contains("Redo")) {
						cmdModifyHexagon = (CmdModifyHexagon) redoStack.peek();
						cmdModifyHexagon.execute(); 
						redoStack.pop();
						undoStack.push(cmdModifyHexagon);
						frame.getTextArea().append("Redo " + cmdModifyHexagon.toString());
					} else {
						shape = selectedShapes.get(0);
						Point center = new Point();
						int radius, edgeColor, innerColor;
						
						center.setX(Integer.parseInt(row.substring(findIndexOf(4, '(', row) + 1, findIndexOf(5, ',', row))));
						center.setY(Integer.parseInt(row.substring(findIndexOf(5, ',', row) + 2, findIndexOf(4, ')', row))));
						radius = (Integer.parseInt(row.substring(findIndexOf(2, '=', row) + 1, findIndexOf(7, ',', row))));
						edgeColor = (Integer.parseInt(row.substring(findIndexOf(5, '(', row) + 1, findIndexOf(5, ')', row))));
						innerColor = (Integer.parseInt(row.substring(findIndexOf(6, '(', row) + 1, findIndexOf(6, ')', row))));
						
						HexagonAdapter newHexagon = new HexagonAdapter(center, radius, new Color(edgeColor), new Color(innerColor));
						
						cmdModifyHexagon = new CmdModifyHexagon((HexagonAdapter) shape, newHexagon);
						cmdModifyHexagon.execute();
						undoStack.push(cmdModifyHexagon);
						redoStack.clear();
						frame.getTextArea().append(cmdModifyHexagon.toString());
					}
				} else if (shape instanceof Rectangle) {
					CmdModifyRectangle cmdModifyRectangle;
					
					if (row.contains("Undo")) {
						cmdModifyRectangle = (CmdModifyRectangle) undoStack.peek();
						cmdModifyRectangle.unexecute();
						undoStack.pop();
						redoStack.push(cmdModifyRectangle);
						frame.getTextArea().append(cmdModifyRectangle.toString());
					} else if (row.contains("Redo")) {
						cmdModifyRectangle = (CmdModifyRectangle) redoStack.peek();
						cmdModifyRectangle.execute(); 
						redoStack.pop();
						undoStack.push(cmdModifyRectangle);
						frame.getTextArea().append("Redo " + cmdModifyRectangle.toString());
					} else {
						shape = selectedShapes.get(0);
						Point upperLeftPoint = new Point();
						int width, height, edgeColor, innerColor;
						
						upperLeftPoint.setX(Integer.parseInt(row.substring(findIndexOf(4, '(', row) + 1, findIndexOf(6, ',', row))));
						upperLeftPoint.setY(Integer.parseInt(row.substring(findIndexOf(6, ',', row) + 2, findIndexOf(4, ')', row))));
						
						width = Integer.parseInt(row.substring(findIndexOf(3, '=', row) + 1, findIndexOf(8, ',', row)));
						height = Integer.parseInt(row.substring(findIndexOf(4, '=', row) + 1, findIndexOf(9, '=', row)));
						
						innerColor = Integer.parseInt(row.substring(findIndexOf(5, '(', row) + 1, findIndexOf(5, ')', row)));
						edgeColor = Integer.parseInt(row.substring(findIndexOf(6, '(', row) + 1, findIndexOf(6, ')', row)));						
					
						Rectangle newRectangle = new Rectangle(upperLeftPoint, width, height, new Color(edgeColor), new Color(innerColor));
						
						cmdModifyRectangle = new CmdModifyRectangle((Rectangle) shape, newRectangle);
						cmdModifyRectangle.execute(); 
						undoStack.push(cmdModifyRectangle);
						redoStack.clear();
						frame.getTextArea().append(cmdModifyRectangle.toString());
					}
				} else if (shape instanceof Donut) {
					CmdModifyDonut cmdModifyDonut;
					
					if(row.contains("Undo")) {
						cmdModifyDonut = (CmdModifyDonut) undoStack.peek();
						cmdModifyDonut.unexecute(); 
						undoStack.pop();
						redoStack.push(cmdModifyDonut);
						frame.getTextArea().append("Undo " + cmdModifyDonut.toString());
					} else if (row.contains("Redo")) {
						cmdModifyDonut = (CmdModifyDonut) redoStack.peek();
						cmdModifyDonut.execute(); 
						redoStack.pop();
						undoStack.push(cmdModifyDonut);
						frame.getTextArea().append("Redo " + cmdModifyDonut.toString());
					} else {
						shape = selectedShapes.get(0);
						Point center = new Point();
						int radius, innerRadius, edgeColor, innerColor;
						
						center.setX(Integer.parseInt(row.substring(findIndexOf(4, '(', row) + 1, findIndexOf(6, ',', row))));
						center.setY(Integer.parseInt(row.substring(findIndexOf(6, ',', row) + 2, findIndexOf(4, ')', row))));
						
						radius = Integer.parseInt(row.substring(findIndexOf(3, '=', row) + 1, findIndexOf(8, ',', row)));
						innerRadius = Integer.parseInt(row.substring(findIndexOf(4, '=', row) + 1, findIndexOf(9, ',', row)));
						
						edgeColor = Integer.parseInt(row.substring(findIndexOf(5, '(', row) + 1, findIndexOf(5, ')', row)));
						innerColor = Integer.parseInt(row.substring(findIndexOf(6, '(', row) + 1, findIndexOf(6, ')', row)));
						
						Donut newDonut = new Donut(center, radius, innerRadius, new Color(edgeColor), new Color(innerColor));
						
						cmdModifyDonut = new CmdModifyDonut((Donut) shape, newDonut);
						cmdModifyDonut.execute(); 
						undoStack.push(cmdModifyDonut);
						redoStack.clear();
						frame.getTextArea().append(cmdModifyDonut.toString());	
					}
				} else if (shape instanceof Circle) {
					CmdModifyCircle cmdModifyCircle;
					
					if(row.contains("Undo")) {
						cmdModifyCircle = (CmdModifyCircle) undoStack.peek();
						cmdModifyCircle.unexecute(); 
						undoStack.pop();
						redoStack.push(cmdModifyCircle);
						frame.getTextArea().append("Undo " + cmdModifyCircle.toString());
					} else if (row.contains("Redo")) {
						cmdModifyCircle = (CmdModifyCircle) redoStack.peek();
						cmdModifyCircle.execute(); 
						redoStack.pop();
						undoStack.push(cmdModifyCircle);
						frame.getTextArea().append("Redo " + cmdModifyCircle.toString());
					} else {
						shape = selectedShapes.get(0);
						Point center = new Point();
						int radius, edgeColor, innerColor;
						
						center.setX(Integer.parseInt(row.substring(findIndexOf(3, '(', row) + 1, findIndexOf(5, ',', row))));
						center.setY(Integer.parseInt(row.substring(findIndexOf(5, ',', row) + 2, findIndexOf(3, ')', row))));
						radius = Integer.parseInt(row.substring(findIndexOf(2, '=', row) + 1, findIndexOf(7, ',', row) + 1));
						edgeColor = Integer.parseInt(row.substring(findIndexOf(5, '(', row) + 1, findIndexOf(5, ')', row)));
						innerColor = Integer.parseInt(row.substring(findIndexOf(6, '(', row) + 1, findIndexOf(6, ')', row)));
						
						Circle newCircle = new Circle(center, radius, new Color(edgeColor), new Color(innerColor));
						
						cmdModifyCircle = new CmdModifyCircle((Circle) shape, newCircle);
						cmdModifyCircle.execute(); 
						undoStack.push(cmdModifyCircle);
						redoStack.clear();
						frame.getTextArea().append(cmdModifyCircle.toString());
					}
				}
			} else if (row.contains("Removed")) {
				CmdRemoveShape cmdRemoveShape;
				
				if(row.contains("Undo")) {
					cmdRemoveShape = (CmdRemoveShape) undoStack.peek();
					cmdRemoveShape.unexecute();
					redoShapes.add(undoShapes.get(undoShapes.size() - 1));
					selectedShapes.add(undoShapes.get(undoShapes.size() - 1));
					undoShapes.remove(undoShapes.size() - 1);
					undoStack.pop();
					redoStack.push(cmdRemoveShape);
					frame.getTextArea().append("Undo " + cmdRemoveShape.toString());
				} else if (row.contains("Redo")) {
					cmdRemoveShape = (CmdRemoveShape) redoStack.peek();
					cmdRemoveShape.execute();
					undoShapes.add(redoShapes.get(redoShapes.size() - 1));
					selectedShapes.remove(redoShapes.get(redoShapes.size() - 1));
					redoShapes.remove(redoShapes.size() - 1);
					redoStack.pop();
					undoStack.push(cmdRemoveShape);
					frame.getTextArea().append("Redo " + cmdRemoveShape.toString());
				} else {
					shape = selectedShapes.get(0);
					cmdRemoveShape = new CmdRemoveShape(model, shape, model.getShapes().indexOf(shape));
					cmdRemoveShape.execute();
					selectedShapes.remove(shape);
					selectedShapes.add(shape);
					undoShapes.add(shape);
					undoStack.push(cmdRemoveShape);
					redoStack.clear();
					frame.getTextArea().append(cmdRemoveShape.toString());
				} 
			} else if (row.contains("Moved to back")) {
					CmdToBack cmdToBack;
					
					if (row.contains("Undo")) {
						cmdToBack = (CmdToBack) undoStack.peek();
						cmdToBack.unexecute();
						
						undoStack.pop();
						redoStack.push(cmdToBack);
						
						frame.getTextArea().append("Undo " + cmdToBack.toString());
					} else if (row.contains("Redo")) {
						cmdToBack = (CmdToBack) redoStack.peek();
						cmdToBack.execute();
						
						redoStack.pop();
						undoStack.push(cmdToBack);
						
						frame.getTextArea().append("Redo " + cmdToBack.toString());
					} else {
						shape = selectedShapes.get(0);
						
						cmdToBack = new CmdToBack(model, shape);
						cmdToBack.execute(); 
						
						undoStack.push(cmdToBack);
						redoStack.clear();
						frame.getTextArea().append(cmdToBack.toString());
					}
				} else if (row.contains("Moved to front")) {
					CmdToFront cmdToFront;
					
					if(row.contains("Undo")) {
						cmdToFront = (CmdToFront) undoStack.peek();
						cmdToFront.unexecute(); 
						
						undoStack.pop();
						redoStack.push(cmdToFront);
						
						frame.getTextArea().append("Undo " + cmdToFront.toString());
					} else if (row.contains("Redo")) {
						cmdToFront = (CmdToFront) redoStack.peek();
						cmdToFront.execute(); 
						
						redoStack.pop();
						undoStack.push(cmdToFront);
						
						frame.getTextArea().append("Redo " + cmdToFront.toString());
					} else {
						shape = selectedShapes.get(0);
						cmdToFront = new CmdToFront(model, shape);
						cmdToFront.execute(); 
						
						undoStack.push(cmdToFront);
						redoStack.clear();
						
						frame.getTextArea().append(cmdToFront.toString());
					}
				} else if (row.contains("Brought to back")) {
					CmdBringToBack cmdBringToBack;

					if (row.contains("Undo")) {
						cmdBringToBack = (CmdBringToBack) undoStack.peek();
						cmdBringToBack.unexecute();
						
						undoStack.pop();
						redoStack.push(cmdBringToBack);
						
						frame.getTextArea().append("Undo " + cmdBringToBack.toString());
					} else if (row.contains("Redo")) {
						cmdBringToBack = (CmdBringToBack) redoStack.peek();
						cmdBringToBack.execute();
						
						redoStack.pop();
						undoStack.push(cmdBringToBack);
						
						frame.getTextArea().append("Redo " + cmdBringToBack.toString());
					} else {
						shape = selectedShapes.get(0);
						
						cmdBringToBack = new CmdBringToBack(model, shape);
						cmdBringToBack.execute();
						
						undoStack.push(cmdBringToBack);
						redoStack.clear();
						
						frame.getTextArea().append(cmdBringToBack.toString());
					}
				} else if (row.contains("Brought to front")) {
					CmdBringToFront cmdBringToFront;

					if (row.contains("Undo")) {
						cmdBringToFront = (CmdBringToFront) undoStack.peek();
						cmdBringToFront.unexecute();
						
						undoStack.pop();
						redoStack.push(cmdBringToFront);
						
						frame.getTextArea().append("Undo " + cmdBringToFront.toString());
					} else if (row.contains("Redo")) {
						cmdBringToFront = (CmdBringToFront) redoStack.peek();
						cmdBringToFront.execute();
						
						redoStack.pop();
						undoStack.push(cmdBringToFront);
						
						frame.getTextArea().append("Redo " + cmdBringToFront.toString());
					} else {
						shape = selectedShapes.get(0);
						
						cmdBringToFront = new CmdBringToFront(model, shape);
						cmdBringToFront.execute();
						
						undoStack.push(cmdBringToFront);
						redoStack.clear();
						
						frame.getTextArea().append(cmdBringToFront.toString());
					}
				} 
			
			
			
			
			
			
			logCounter++;
			frame.getView().repaint();
			//frame.repaint();
		} else {
			frame.getBtnLoadNext().setEnabled(false);
			frame.getTglBtnPoint().setEnabled(true);
			frame.getTglBtnLine().setEnabled(true);
			frame.getTglBtnCircle().setEnabled(true);
			frame.getTglBtnDonut().setEnabled(true);
			frame.getTglBtnRectangle().setEnabled(true);
			frame.getTglBtnHexagon().setEnabled(true);
			frame.getBtnUndo().setEnabled(false);
			enableDisableButtons();

		}
	}
	
	
	
	

	public void undoRedoButtons() {
		if (undoCounter < 1) {
			frame.getBtnUndo().setEnabled(false);
		} else {
			frame.getBtnUndo().setEnabled(true);
		}

		if (redoCounter < 1 || redoStack.isEmpty()) {
			frame.getBtnRedo().setEnabled(false);
		} else {
			frame.getBtnRedo().setEnabled(true);
		}
	}

	public void undo() {
		command = undoStack.peek();
		command.unexecute();
		int index = undoShapes.size() - 1;
		
		if(command instanceof CmdRemoveShape)
		{
			redoShapes.add(undoShapes.get(index));
			selectedShapes.add(undoShapes.get(index));
			undoShapes.remove(index);
		}
		frame.getTextArea().append("Undo " + undoStack.peek().toString());
		
		redoCounter++;
		undoCounter--;
		
		
		
		frame.repaint();
		undoStack.pop();
		redoStack.push(command);
		
		undoRedoButtons();
		enableDisableButtons();

		
	}


	public void redo() {
		command = redoStack.peek();
		command.execute();
		int index = redoShapes.size() - 1;
		if(command instanceof CmdRemoveShape)
		{
			undoShapes.add(redoShapes.get(index));
			selectedShapes.add(redoShapes.get(index));
			redoShapes.remove(index);
		}
		frame.getTextArea().append("Redo " + redoStack.peek().toString());
		
		redoCounter--;
		undoCounter++;
		
		frame.repaint();
		redoStack.pop();
		undoStack.push(command);
		
		undoRedoButtons();
		enableDisableButtons();

		
	}
	
	public void toFront() {
		Shape shape = testShape;
		CmdToFront cmdToFront = new CmdToFront(model, shape);
		cmdToFront.execute();
		frame.getTextArea().append(cmdToFront.toString());
		undoStack.push(cmdToFront);
		undoCounter++;
		redoStack.clear();
		undoRedoButtons();
		enableDisableButtons();


		frame.getView().repaint();
		//frame.repaint();
	}
	
	public void toBack() {
		Shape shape = testShape;
		CmdToBack cmdToBack = new CmdToBack(model, shape);
		cmdToBack.execute();
		
		frame.getTextArea().append(cmdToBack.toString());
		
		undoStack.push(cmdToBack);
		undoCounter++;
		redoStack.clear();
		undoRedoButtons();
		enableDisableButtons();

		frame.getView().repaint();
		//frame.repaint();
	}
	
	public void bringToFront() {
		Shape shape = testShape;
		
		CmdBringToFront cmdBringToFront = new CmdBringToFront(model, shape);
		cmdBringToFront.execute();
		
		frame.getTextArea().append(cmdBringToFront.toString());
		undoStack.push(cmdBringToFront);
		undoCounter++;
		redoStack.clear();
		
		undoRedoButtons();
		enableDisableButtons();

		
		frame.getView().repaint();
		//frame.repaint();
	}
	
	
	public void bringToBack() {
		Shape shape = testShape;
		
		CmdBringToBack cmdBringToBack = new CmdBringToBack(model, shape);
		cmdBringToBack.execute();
		
		frame.getTextArea().append(cmdBringToBack.toString());
		undoStack.push(cmdBringToBack);
		undoCounter++;
		redoStack.clear();
		undoRedoButtons();
		enableDisableButtons();

		frame.getView().repaint();
		//frame.repaint();
	}
	

	
	public void enableDisableButtons() {

		if (model.getShapes().size() != 0) {
			btnObserver.setSelectBtnActivated(true);
			if (selectedShapes.size() != 0) {
				if (selectedShapes.size() == 1)// 1
				{
					btnObserver.setModifyBtnActivated(true);

				} else {
					btnObserver.setModifyBtnActivated(false);
		}
				btnObserver.setBtnDeleteActivated(true);
			} else {
				btnObserver.setModifyBtnActivated(false);
				btnObserver.setBtnDeleteActivated(false);

			}
		} else {
			btnObserver.setSelectBtnActivated(false);
			btnObserver.setModifyBtnActivated(false);
			btnObserver.setBtnDeleteActivated(false);

		}

	}
	

	
	public int findIndexOf(int n, char c, String s) {
        int occurr = 0;
        for(int i = 0; i < s.length(); i++) {
            if(s.charAt(i) == c) {
                occurr += 1;
            }
            if(occurr == n) {
                return i;
            }
        }
        return -1;
    }
	
	

}
