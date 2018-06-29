package eDLineEditor;


import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EDLineEditor {
	
	/**
	 * 接收用户控制台的输入，解析命令，根据命令参数做出相应处理。
	 * 不需要任何提示输入，不要输出任何额外的内容。
	 * 输出换行时，使用System.out.println()。或者换行符使用System.getProperty("line.separator")。
	 * 
	 * 待测方法为public static void main(String[] args)方法。args不传递参数，所有输入通过命令行进行。
	 * 方便手动运行。
	 * 
	 * 说明：可以添加其他类和方法，但不要删除该文件，改动该方法名和参数，不要改动该文件包名和类名
	 */
	private String ins;
	private String[] instruction;
	private boolean haveQuit = false;
	private boolean haveStore = false;
	private boolean haveChange = false;
	private boolean edfile = false;
	private int quitTimes = 0;
	private int address1;
	private int address2;
	private char order;
	private String param;
	private String lastparam = "";
	private String fileName;
	private ArrayList<String> contents = new ArrayList<>();
	private int currentline;
	private HashMap<Character,Integer> k_index = new HashMap<>();
	private String lineBreak = System.getProperty("line.separator");
	private Scanner in = new Scanner(System.in);
	private char lastorder;
	private Stack<Integer> line_stack = new Stack<>();
	private int count_u = 0;
	private ArrayList<ArrayList<String>> stack = new ArrayList<>();

	public static void main(String[] args) {
		EDLineEditor ed = new EDLineEditor();
		String addressReg = "((?:\\.|\\$|[0-9]+|/[^/]+/|\\?[^?]+\\?|'[a-z]|,|;)?[-+]?(?:[0-9])?)?";
		String paramReg = "([a-z]|/.+/(?:[0-9g])?|(?:(?:\\.|\\$|[0-9]+|/[^/]+/|\\?[^?]+\\?|'[a-z]|,|;)?[-+]?(?:\\.|\\$|[0-9]+|/[^/]+/|\\?[^?]+\\?|'[a-z]|;)?))?";
		String regex = addressReg + ",?" + addressReg + "([a-zA-Z=])" + paramReg;
		Pattern pattern = Pattern.compile(regex);
		EXIT:while(!ed.haveQuit){
			if(ed.in.hasNextLine())
				ed.ins = ed.in.nextLine();
			Matcher matcher = pattern.matcher(ed.ins);
			if(matcher.find()) {
				ed.address1 = ed.getAddress(matcher.group(1),ed);
				ed.address2 = ed.getAddress(matcher.group(2),ed);
				ed.order = matcher.group(3).charAt(0);
				if(ed.order=='w'||ed.order=='W'||ed.order=='f'||ed.order=='e'){
					ed.instruction = ed.ins.split(" ");
					if(ed.instruction.length>1)
						ed.param = ed.instruction[1];
					else
						ed.param = "";
				}
				ed.param = matcher.group(4);
			}
			else
				break;
			switch(ed.order) {
				case 'q':
					if(ed.address1==-1) {
						if (ed.haveChange) {
							if (ed.haveStore)
								break EXIT;
							else {
								ed.quitTimes++;
								if (ed.quitTimes == 1)
									System.out.println("?");
								else
									break EXIT;
							}
						} else
							break EXIT;
					}
					else
						System.out.println("?");
					break;
				case 'Q':
					if(ed.address1!=-1) {
						System.out.println("?");
						break;
					}
					break EXIT;
				case 'e':
					ed.order_ed();
					break;
				case 'f':
					ed.order_f();
					break;
				case 'w':
					ed.order_w();
					break;
				case 'W':
					ed.order_W();
					break;
				case 'a':
					ed.order_a();
					break;
				case 'i':
					ed.order_i();
					break;
				case 'c':
					ed.order_c();
					break;
				case 'd':
					ed.order_d();
					break;
				case 'p':
					ed.order_p();
					break;
				case '=':
					ed.order_equalSymbol();
					break;
				case 'z':
					ed.order_z();
					break;
				case 'm':
					ed.order_m(ed);
					break;
				case 't':
					ed.order_t(ed);
					break;
				case 'j':
					ed.order_j();
					break;
				case 's':
					ed.order_s();
					break;
				case 'k':
					ed.order_k();
					break;
				case 'u':
					ed.order_u();
					ed.haveChange = true;
					break;
			}
			ed.lastorder = ed.order;
		}
	}

	private void order_ed(){
		if (this.instruction.length == 2) {
			this.edfile = true;
			this.fileName = this.instruction[1];
			this.contents.clear();
			try {
				File filename = new File(this.fileName);
				if(!filename.exists())
					filename.createNewFile();
				InputStreamReader reader = new InputStreamReader(new FileInputStream(filename));
				BufferedReader br = new BufferedReader(reader);
				String line;
				while ((line = br.readLine()) != null) {
					this.contents.add(line);
				}
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			this.currentline = this.contents.size();
			this.stack.add(new ArrayList<>(this.contents));
			this.line_stack.push(this.currentline);
		}
		else {
			this.edfile = false;
			this.contents.clear();
			this.currentline = 0;
			this.stack.add(new ArrayList<>(this.contents));
			this.line_stack.push(this.currentline);
		}
	}

	private void order_f(){
		if(this.address1==-1) {
			if (this.instruction.length == 2) {
				this.fileName = this.instruction[1];
			}
			else {
				if (this.fileName!=null)
					System.out.println(this.fileName);
				else {
					System.out.println("?");
				}
			}
		}
		else {
			System.out.println("?");
		}
	}

	private void order_w(){
		String f;
		if(this.instruction.length==2)
			f = this.instruction[1];
		else
			f = this.fileName;
		if(f!=null&&f.length()!=0&&this.param.length()==0){
			try{
				File filename = new File(f);
				if(!filename.exists())
					filename.createNewFile();
				int addr1 = -2;
				int addr2 = -2;
				if(this.address1==-1&&this.address2==-1){
					addr1 = 1;
					addr2 = this.contents.size();
				}
				else if(this.address1>0&&this.address2==-1){
					addr1 = this.address1;
					addr2 = this.address1;
				}
				else if(this.address1>0&&this.address2>0){
					addr1 = this.address1;
					addr2 = this.address2;
				}
				if(addr1>0&&addr2>0){
					BufferedWriter out = new BufferedWriter(new FileWriter(filename));
					String line;
					for(int i=addr1-1; i<addr2; i++){
						line = this.contents.get(i);
						out.write(line + lineBreak);
					}
					out.flush();
					out.close();
				}
				else {
					System.out.println("?");
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
			this.haveStore = true;
		}
		else {
			System.out.println("?");
		}
	}

	private void order_W(){
		String f;
		if(this.instruction.length==2)
			f = this.instruction[1];
		else
			f = this.fileName;
		if(f!=null&&f.length()!=0&&this.param.length()==0){
			try{
				File filename = new File(f);
				if(!filename.exists())
					filename.createNewFile();
				int addr1 = -2;
				int addr2 = -2;
				if(this.address1==-1&&this.address2==-1){
					addr1 = 1;
					addr2 = this.contents.size();
				}
				else if(this.address1>0&&this.address2==-1){
					addr1 = this.address1;
					addr2 = addr1;
				}
				else if(this.address1>0&&this.address2>0){
					addr1 = this.address1;
					addr2 = this.address2;
				}
				if(addr1>0&&addr2>0){
					BufferedWriter out = new BufferedWriter(new FileWriter(filename,true));
					String line;
					for(int i=addr1-1; i<addr2; i++){
						line = this.contents.get(i);
						out.write(line + lineBreak);
					}
					out.flush();
					out.close();
				}
				else {
					System.out.println("?");
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
			this.haveStore = true;
		}
		else {
			System.out.println("?");
		}
	}

	private void order_a(){
		String line;
		if(this.address1==-1&&this.address2==-1) {
			this.line_stack.push(this.currentline);
			while (in.hasNextLine()&&!(line = in.nextLine()).equals(".")) {
				this.contents.add(this.currentline,line);
				this.currentline++;
			}
			this.haveChange = true;
			this.stack.add(new ArrayList<>(this.contents));
		}
		else if(this.address1!=-1&&this.address2==-1&&this.address1!=-2&&this.address1<Integer.MAX_VALUE/2){
			this.line_stack.push(this.currentline);
			int linetotal = 0;
			while (in.hasNextLine()&&!(line = in.nextLine()).equals(".")) {
				this.contents.add(this.address1 + linetotal,line);
				this.currentline = this.address1 + linetotal +1;
				linetotal++;
			}
			this.haveChange = true;
			this.stack.add(new ArrayList<>(this.contents));
		}
		else {
			System.out.println("?");
		}
	}

	private void order_i(){
		String line;
		int addr = -2;
		if(this.address1==-1)
			addr = this.currentline;
		else if(this.address1>=0)
			addr = this.address1;
		if(addr>=0&&addr<Integer.MAX_VALUE){
			this.line_stack.push(this.currentline);
			int linetotal = 0;
			while (in.hasNextLine()&&!(line = in.nextLine()).equals(".")) {
				if(addr==0) {
					this.contents.add(linetotal, line);
					this.currentline = 1 + linetotal;
				}
				else {
					this.contents.add(addr - 1 + linetotal, line);
					this.currentline = addr + linetotal;
				}
				linetotal++;
			}
			this.haveChange = true;
			this.stack.add(new ArrayList<>(this.contents));
		}
		else {
			System.out.println("?");
		}
	}

	private void order_c(){
		String line;
		int addr[] = getValidaddr();
		int addr1 = addr[0];
		int addr2 = addr[1];
		if(addr1>0&&addr2>0){
			this.line_stack.push(this.currentline);
			int k = 0;
			while(in.hasNextLine()&&!(line = in.nextLine()).equals(".")){
				this.contents.add(addr2 + k, line);
				this.currentline = addr2 + 1 + k;
				k++;
			}
			for(int i=addr1-1; i<addr2; i++){
				this.contents.remove(addr1-1);
				this.currentline--;
			}
			this.haveChange = true;
			this.stack.add(new ArrayList<>(this.contents));
		}
		else {
			System.out.println("?");
		}
	}

	private void order_d(){
		int addr[] = getValidaddr();
		int addr1 = addr[0];
		int addr2 = addr[1];
		if(addr1>0&&addr2>0){
			this.line_stack.push(this.currentline);
			if(addr2==this.contents.size())
				this.currentline = addr1 - 1;
			else
				this.currentline = addr1;
			for(int i=addr1-1; i<addr2; i++)
				this.contents.remove(addr1-1);
			this.haveChange = true;
			this.stack.add(new ArrayList<>(this.contents));
		}
		else {
			System.out.println("?");
		}
	}

	private void order_p(){
		if(this.address1==-1&&this.address2==-1){
			System.out.println(this.contents.get(this.currentline-1));
		}
		else if(this.address1 > 0 && this.address2 == -1){
			if(this.address1==Integer.MAX_VALUE){
				for(String i:this.contents)
					System.out.println(i);
				this.currentline = this.contents.size();
			}
			else if(this.address1==Integer.MAX_VALUE/2){
				for(int i=this.currentline-1; i<this.contents.size(); i++){
					System.out.println(this.contents.get(i));
				}
				this.currentline = this.contents.size();
			}
			else{
				System.out.println(this.contents.get(this.address1-1));
				this.currentline = this.address1;
			}
		}
		else if(this.address1 >= 0 && this.address2 >= 0&&this.address1<=this.address2) {
			if (this.address1 == 0) {
				for (int i = this.address1; i < this.address2; i++) {
					System.out.println(this.contents.get(i));
				}
				this.currentline = this.address2;
			} else {
				for (int i = this.address1 - 1; i < this.address2; i++) {
					System.out.println(this.contents.get(i));
				}
				this.currentline = this.address2;
			}
		}
		else {
			System.out.println("?");
		}
	}

	private void order_equalSymbol(){
		if(this.address1==-1&&this.address2==-1){
			System.out.println(this.contents.size());
		}
		else if(this.address1 >= 0 && this.address2 == -1&&this.address1<Integer.MAX_VALUE){
			System.out.println(this.address1);
		}
		else {
			System.out.println("?");
		}
	}

	private void order_z(){
		int n = -1;
		int addr = -2;
		boolean isanum = true;
		for (int i = 0; i < this.param.length(); i++)
			if (!(this.param.charAt(i) >= '0' && this.param.charAt(i) <= '9'))
				isanum = false;
		if(this.param.length()>0&&isanum)
			n = Integer.parseInt(this.param);
		if(this.address1==-1&&this.address2==-1)
			addr = this.currentline + 1;
		else if(this.address1>0&&this.address2==-1)
			addr = this.address1;
		if(addr>0){
			if(n>0) {
				if (addr + n <= this.contents.size()) {
					for (int i = addr - 1; i < addr + n; i++)
						System.out.println(this.contents.get(i));
					this.currentline = addr + n;
				} else {
					for (int i = addr - 1; i < this.contents.size(); i++)
						System.out.println(this.contents.get(i));
					this.currentline = this.contents.size();
				}
			}
			else{
				for(int i=addr-1; i<this.contents.size(); i++)
					System.out.println(this.contents.get(i));
				this.currentline = this.contents.size();
			}
		}
		else {
			System.out.println("?");
		}
	}

	private void order_m(EDLineEditor ed){
		int addr[] = getValidaddr();
		int addr1 = addr[0];
		int addr2 = addr[1];
		int addr3 = getAddress(this.param,ed);
		if(addr3==-1)
			addr3 = this.currentline;
		if(addr1>0&&addr3>0&&addr1<=addr2&&(addr3<addr1||addr3>=addr2)&&addr3<Integer.MAX_VALUE/2){
			this.line_stack.push(this.currentline);
			int k = 0;
			if(addr3<addr1){
				for(int i=addr1-1; i<addr2; i++){
					this.contents.add(addr3 + k, this.contents.get(i+k));
					this.currentline = addr3 + k + 1;
					k++;
				}
				for(int i=addr1-1+k; i<addr2+k; i++){
					this.contents.remove(addr1-1+k);
				}
			}
			else{
				for(int i=addr1-1; i<addr2; i++){
					this.contents.add(addr3 + k, this.contents.get(i));
					this.currentline = addr3 + k + 1;
					k++;
				}
				for(int i=addr1-1; i<addr2; i++){
					this.contents.remove(addr1-1);
					this.currentline--;
				}
			}
			this.haveChange = true;
			this.stack.add(new ArrayList<>(this.contents));
		}
		else {
			System.out.println("?");
		}
	}

	private void order_t(EDLineEditor ed){
		int addr[] = getValidaddr();
		int addr1 = addr[0];
		int addr2 = addr[1];
		int addr3 = getAddress(this.param,ed);
		if(addr3==-1)
			addr3 = this.currentline;
		if(addr1>0&&addr3>=0&&addr1<=addr2&&addr3<Integer.MAX_VALUE/2) {
			this.line_stack.push(this.currentline);
			int k = 0;
			ArrayList<String> adds = new ArrayList<>();
			for(int i = addr1-1; i<addr2; i++)
				adds.add(this.contents.get(i));
			for (int i = addr1 - 1; i < addr2; i++) {
				this.contents.add(addr3 + k, adds.get(k));
				this.currentline = addr3 + k + 1;
				k++;
			}
			this.haveChange = true;
			this.stack.add(new ArrayList<>(this.contents));
		}
		else {
			System.out.println("?");
		}
	}

	private void order_j(){
		if(this.param==null||this.param.length()==0){
			int addr1 = -2;
			int addr2 = -2;
			String adds = "";
			if(this.address1==-1&&this.address2==-1){
				addr1 = this.currentline;
				addr2 = this.currentline + 1;
			}
			else if(this.address1 > 0 && this.address2 > 0){
				addr1 = this.address1;
				addr2 = this.address2;
			}
			else if(this.address1>0&&this.address2==-1){
				if(this.address1==Integer.MAX_VALUE){
					addr1 = 1;
					addr2 = this.contents.size();
				}
				else if(this.address1==Integer.MAX_VALUE/2){
					addr1 = this.currentline;
					addr2 = this.contents.size();
				}
			}
			if(addr1<=addr2&&addr1>0&&addr2<=this.contents.size()) {
				this.line_stack.push(this.currentline);
				for (int i = addr1 - 1; i < addr2; i++) {
					adds = adds + this.contents.get(addr1 - 1);
					this.contents.remove(addr1 - 1);
				}
				this.contents.add(addr1 - 1, adds);
				this.currentline = addr1;
				this.haveChange = true;
				this.stack.add(new ArrayList<>(this.contents));
			}
			else {
				System.out.println("?");
			}
		}
		else {
			System.out.println("?");
		}
	}

	private void order_s(){
		int addr[] = getValidaddr();
		int addr1 = addr[0];
		int addr2 = addr[1];
		if(this.param!=null&&this.param.length()!=0)
			this.lastparam = this.param;
		else
			this.param = this.lastparam;
		if(addr1 >0&&addr1<=addr2&&(this.param!=null&&this.param.length()!=0)) {
			this.line_stack.push(this.currentline);
			String[] str = this.param.split("/", -1);
			int change;
			if(str[3].length()==0)
				change = 1;
			else if(str[3].equals("g"))
				change = Integer.MAX_VALUE;
			else
				change = Integer.parseInt(str[3]);
			String newstr;
			boolean havefind = false;
			if (change == Integer.MAX_VALUE) {
				for(int i=addr1-1; i<addr2; i++){
					if(this.contents.get(i).contains(str[1])) {
						havefind = true;
						newstr = this.contents.get(i).replace(str[1], str[2]);
						this.contents.set(i, newstr);
						this.currentline = i + 1;
						this.haveChange = true;
					}
				}
				if(!havefind) {
					System.out.println("?");
					this.line_stack.pop();
				}
				else
					this.stack.add(new ArrayList<>(this.contents));
				newstr = "";
			}
			else{
				String tofind;
				int times;
				for(int i=addr1-1; i<addr2; i++){
					tofind = this.contents.get(i);
					times = 0;
					while(tofind.length()>0&&tofind.contains(str[1])){
						times++;
						if(times==change){
							havefind = true;
							tofind = tofind.substring(tofind.indexOf(str[1]), tofind.length());
							newstr = str[2] + tofind.substring(str[1].length(),tofind.length());
							newstr = this.contents.get(i).substring(0,this.contents.get(i).length()-tofind.length()) + newstr;
							this.contents.add(i + 1,newstr);
							this.contents.remove(i);
							this.currentline = i + 1;
							this.haveChange = true;
							break;
						}
						tofind = tofind.substring(tofind.indexOf(str[1]) + str[1].length(), tofind.length());
					}
				}
				if(!havefind) {
					System.out.println("?");
					this.line_stack.pop();
				}
				else
					this.stack.add(new ArrayList<>(this.contents));
				newstr = "";
				tofind = "";
			}
		}
		else {
			System.out.println("?");
		}
	}

	private void order_k(){
		int addr = -2;
		if(this.address1==-1&&this.address2==-1)
			addr = this.currentline;
		else if(this.address1 > 0 && this.address2 == -1)
			addr = this.address1;
		if(this.param.length()==1&&this.param.charAt(0)>='a'&&this.param.charAt(0)<='z'&&addr>0){
			this.k_index.put(this.param.charAt(0),addr);
		}
		else {
			System.out.println("?");
		}
	}  //未实现

	private void order_u(){
		if(this.address1==-1&&this.address2==-1&&this.param.length()==0) {
			if ((this.lastorder != 'u'&&this.lastorder!='p')||this.count_u==0) {
				this.stack.remove(this.stack.size()-1);
			}
			this.contents.clear();
			this.contents.addAll(this.stack.get(this.stack.size()-1));
			this.stack.remove(this.stack.size()-1);
			this.currentline = this.line_stack.pop();
			this.count_u++;
		}
		else
			System.out.println("?");
	}

	private int getAddress(String address,EDLineEditor ed) {
		int num;
		if (address != null&&address.length()!=0) {
			String regex = "(\\.|\\$|[0-9]+|/[^/]+/|\\?[^?]+\\?|'[a-z]|,|;)?([-+])?(\\.|\\$|[0-9]+|/[^/]+/|\\?[^?]+\\?|'[a-z])?";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(address);
			String param1 = null;
			String param2 = null;
			String param3 = null;
			if (matcher.find()) {
				param1 = matcher.group(1);
				param2 = matcher.group(2);
				param3 = matcher.group(3);
			}
			int num1;
			int num2;
			if(param1!=null)
				num1 = ed.transToInt(param1);
			else
				num1 = -1;
			if(param3!=null)
				num2 = ed.transToInt(param3);
			else
				num2 = -1;
			if(num1>=-1&&num2>=-1) {
				if (param2 != null) {
					if(num1<Integer.MAX_VALUE/2&&num2<Integer.MAX_VALUE/2){
						if (param2.equals("+")) {
							if (param1 != null)
								num = num1 + num2;
							else
								num = this.currentline + num2;
							if (num > ed.contents.size())
								num = -2;
						} else {
							if (param1 != null)
								num = num1 - num2;
							else
								num = this.currentline - num2;
							if (num < 0)
								num = -2;
						}
					}
					else
						num = -2;
				}
				else {
					num = num1;
				}
			}
			else
				num = -2;
		}
		else
			num = -1;
		return num;
	}

	private int transToInt(String param){
		int toint = -2;
		boolean isanum = true;
		for(int i=0; i<param.length(); i++)
			if(!(param.charAt(i)>='0'&&param.charAt(i)<='9'))
				isanum = false;
		if(param.equals(".")){
			toint = this.currentline;
		}
		else if(param.equals("$")){
			toint = this.contents.size();
		}
		else if(param.equals(",")){
			toint  = Integer.MAX_VALUE;
		}
		else if(param.equals(";")){
			toint  = Integer.MAX_VALUE/2;
		}
		else if(param.startsWith("/")){
			boolean havefind = false;
			String temp;
			for(int i=this.currentline; i<this.contents.size(); i++){
				temp = this.contents.get(i);
				if(temp.contains(param.substring(1,param.length()-1))){
					toint = i + 1;
					havefind = true;
					break;
				}
			}
			if(!havefind){
				for(int i=0; i<this.currentline; i++){
					temp = this.contents.get(i);
					if(temp.contains(param.substring(1,param.length()-1))){
						toint = i + 1;
						break;
					}
				}
			}
		}
		else if(param.startsWith("?")){
			boolean havefind = false;
			String temp;
			for(int i=this.currentline-2; i>=0; i--){
				temp = this.contents.get(i);
				if(temp.contains(param.substring(1,param.length()-1))){
					toint = i + 1;
					havefind = true;
					break;
				}
			}
			if(!havefind){
				for(int i=this.contents.size()-1; i>=this.currentline-1; i--){
					temp = this.contents.get(i);
					if(temp.contains(param.substring(1,param.length()-1))){
						toint = i + 1;
						break;
					}
				}
			}
		}
		else if(isanum) {
			toint = Integer.parseInt(param);
			if(toint<0||toint>this.contents.size())
				toint = -2;
		}
		else if(param.startsWith("'"))
			toint = this.k_index.getOrDefault(param.charAt(1),-2);
		else
			toint = -2;
		return toint;
	}

	private int[] getValidaddr(){
		int[] addrs = new int[]{-2,-2};
		if(this.address1==-1&&this.address2==-1){
			addrs[0] = this.currentline;
			addrs[1] = this.currentline;
		}
		else if(this.address1>0&&this.address2==-1){
			if(this.address1==Integer.MAX_VALUE){
				addrs[0] = 1;
				addrs[1] = this.contents.size();
			}
			else if(this.address1==Integer.MAX_VALUE/2){
				addrs[0] = this.currentline;
				addrs[1] = this.contents.size();
			}
			else{
				addrs[0] = this.address1;
				addrs[1] = this.address1;
			}
		}
		else if(this.address1>0&&this.address2>0){
			addrs[0] = this.address1;
			addrs[1] = this.address2;
		}
		return addrs;
	}
	
}
