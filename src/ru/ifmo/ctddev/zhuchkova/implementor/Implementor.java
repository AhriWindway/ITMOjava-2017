package ru.ifmo.ctddev.zhuchkova.implementor;

import info.kgeorgiy.java.advanced.implementor.*;
import java.io.*;
import java.nio.file.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.*;
import java.util.*;
import java.util.jar.*;
import javax.tools.*;
import java.util.zip.ZipEntry;

/**
 * Implements given type-token and stores it to .java/.jar file
 *
 * @author Anastasia Zhuchkova
 */

public class Implementor implements JarImpler {
  /**
   * String representation of implemented class name
   */
  private String parentClassName = null;
  /**
   * String representation of resulting class name
   */
  private String className = null;

  /**
   * System line separator
   */
  private static final String LS = System.getProperty("line.separator");
  /**
   * System file separator
   */
  private static final String FS = System.getProperty("file.separator");
  /**
   * System path separator
   */
  private static final String PS = System.getProperty("path.separator");
  /**
   * Main {@link java.lang.StringBuilder} object, containing result
   */
  private StringBuilder code;
  /**
   * Boolean value, true if we implemented default constructor in our class 
   */
  private boolean implementedDefault;
  /**
   * Boolean value, if token has default constructor
   */
  private boolean foundSuper;
  /**
   * Array of ancestor`s constructors
   */
  private Constructor<?>[] constructors;
  /**
   * Super constructor to our class
   */
  private Constructor<?> superCons;
  /**
   * All packages to import
   */
  private Set<Package> packages = new HashSet<>();
  /**
   * All methods in all ancestors
   */
  private Map<String, Method> allMethods = new HashMap<>();
  /**
   * All methods we need to implement
   */
  private Map<String, Method> methods = new HashMap<>();
  /**
   * Descriptor of file where to save implementation
   */
  private File targetFile;
  /**
   * Parent class package
   */
  private String pack;
  
  
  /**
   * Returns string representation of {@link java.lang.reflect.Modifier}
   *
   * @param m modifier in int
   *
   * @return {@link java.lang.String}
   */
  private String modifiers(int m) {
      return Modifier.toString(m);
  }
  
  /**
   * Recursively deletes directory by given file descriptor
   *
   * @param directory {@link java.io.File}
   */
  private void deleteDir(File directory) {
    if (directory.isDirectory()) {
      for (File c : directory.listFiles())
          deleteDir(c);
      directory.delete();
    } else {
      if ((!directory.delete()) && (directory.isDirectory())) {
        System.err.println("Failed to delete directory: " + directory);
      }
    }
  }
  
  /**
   * Entry point
   *
   * @param args command line args
   */
  public static void main(String[] args) {
    try {
      if ((args.length == 2) && (args[0].equals("-jar"))) {
        new Implementor().implementJar(Class.forName(args[1]), Paths.get("lib" + FS + Class.forName(args[1]).getSimpleName() + "Impl.jar"));
      } else {
        if (args.length == 1) new Implementor().implement(Class.forName(args[0]), Paths.get("."));
        else System.out.println("Invalid arguments.");
      }
    } catch (ClassNotFoundException e) {
      System.out.print("Cannot find class with name " + args[0] + " ");
    } catch (ImplerException e) {
      System.out.println("Cannot generate default implementation for class " + args[0]);
    } 
  }
  
  /**
    * Initializes all fields required to correct work
    *
    * @param token class token
    * @param root  root directory.
    */
  private void init(Class<?> token, Path root) {
    code = new StringBuilder();
    implementedDefault = true;
    foundSuper = false;
    constructors = null;
    superCons = null;
    packages.clear();
    allMethods.clear();
    methods.clear();
    pack = "";
    if (token.getPackage() != null) pack = token.getPackage().getName();
    targetFile = new File(root.toString() + FS + pack.replace(".", FS) + FS + token.getSimpleName() + "Impl.java");
  }
  

  /**
    * Implements class by given type-token and puts it in .jar file
    *
    * @param token   type token to create implementation for.
    * @param jarFile target <tt>.jar</tt> file.
    *
    * @throws ImplerException then something with implementation goes wrong
    */
  @Override
  public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
    try {
      jarFile.toFile().getParentFile().mkdirs();
      jarFile.toFile().createNewFile();
        
      String tempFileName = "tmp" + FS;
      implement(token, Paths.get(tempFileName));

      ToolProvider.getSystemJavaCompiler().run(null, null, null, targetFile.getPath());
      File compiledClass = new File(targetFile.getPath().replace(".java", ".class"));

      Manifest manifest = new Manifest();
      manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
      manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, pack + "." + className);
      
      try(JarOutputStream jOut = new JarOutputStream(new FileOutputStream(jarFile.toFile()), manifest);
          BufferedInputStream bis = new BufferedInputStream(new FileInputStream(compiledClass))) {
        jOut.putNextEntry(new ZipEntry(compiledClass.getPath().replace(tempFileName, "")));
        int bytesRead;
        byte[] buffer = new byte[8 * 1024];
        
        while ((bytesRead = bis.read(buffer)) != -1) 
          jOut.write(buffer, 0, bytesRead);
        deleteDir(new File(tempFileName));
      }
    } catch (FileNotFoundException e) {
        System.out.println("Jar file doesnt exist");
    } catch (IOException e) {
        System.out.println("Problems with jar");
    }
  }
  
  /**
    * Implements or extends interface/class of given type-token and names it like "ParentNameImpl.java"
    *
    * @param token   type token to create implementation for.
    * @param root where to store implementation
    *
    * @throws ImplerException then something with implementation goes wrong
    */
  @Override
  public void implement(Class<?> token, Path root) throws ImplerException {
    init(token, root);
    
    if (Enum.class.isAssignableFrom(token)) 
      throw new ImplerException("Can't implement enum class");
    
    if (Modifier.isFinal(token.getModifiers())) 
      throw new ImplerException("Can't implement final class");
    
    File parentFile = targetFile.getParentFile();
    if (!parentFile.exists() && !parentFile.mkdirs()) 
      throw new ImplerException("Can't create dir: " + parentFile);
    
    
    try (PrintWriter out = new PrintWriter(targetFile)) { 
      parentClassName = token.getSimpleName();
      className = parentClassName + "Impl";
      
      if (!pack.isEmpty()) 
        code.append("package ").append(pack).append(";").append(LS);
        
      constructors = token.getDeclaredConstructors();
      getMethods(token);
      
      for (Method method : methods.values()) {
        for (Class<?> param: method.getParameterTypes()) 
          packages.add(param.getPackage());
      }

      for (Constructor<?> constructor : constructors) {
        if ((!Modifier.isPrivate(constructor.getModifiers())) && (!foundSuper)) {
          superCons = constructor;
          foundSuper = true;
        }
        
        for (Class<?> param: constructor.getParameterTypes()) 
          packages.add(param.getPackage());
      }

      for (Package p : packages) 
        if (p != null) code.append("import ").append(p.getName()).append(".*;").append(LS);
        
      code.append("public class ").append(className);

      if (token.isInterface()) code.append(" implements ");
        else code.append(" extends ");
      code.append(token.getName()).append(" {").append(LS);
      
      for (Constructor<?> constructor : constructors) {
        if (constructor.getParameterTypes().length == 0) 
            implementedDefault = true;
        implementConstructor(constructor);
      }

      if (!implementedDefault) generateDefaultConstructor();
      for (Method m : methods.values()) implementMethod(m);
      
      code.append("}");
      out.print(code);
    } catch (FileNotFoundException e) {
      System.out.println("File not found");
    }
  }
  
  /**
    * Generates default constructor code and appends it to result
    *
    * @throws ImplerException if could't generate super constructor call
    */
  private void generateDefaultConstructor() throws ImplerException {
    code.append("public ").append(className).append("() ");
    if (constructors.length > 0) 
        generateThrows(superCons.getExceptionTypes());
    code.append(" {").append(LS);
    generateSuperCall(superCons);
    code.append("}").append(LS);
  }
  
  /**
    * Recoursive method that collects all methods to implement from given class-token and puts it to {@link #methods}
    *
    * @param token from what token to collect
    */
  private void getMethods(Class<?> token) {
    if (token == Object.class) return;
    if (token == null) return;
    
		for (Method method : token.getDeclaredMethods()) {
			String signature = getMethodSignature(method);
			if (!allMethods.containsKey (signature)) {
				if (Modifier.isAbstract (method.getModifiers ()) && !Modifier.isFinal (method.getModifiers ())
			  			&& !Modifier.isPrivate (method.getModifiers ())) {
          methods.put(signature, method);
				}

				allMethods.put (signature, method);
			}
		}

    getMethods(token.getSuperclass());
    for (Class<?> intface : token.getInterfaces()) 
      getMethods(intface);

  }
  
  /**
    * Generates method signature for given {@link java.lang.reflect.Method} instance.
    *
    * @param method {@link java.lang.reflect.Method} signature to generate
    *
    * @return {@link java.lang.String} with method signature
    */
  private String getMethodSignature(Method method) {
    StringBuilder sb = new StringBuilder();
    sb.append(method.getName()).append("(");
    Class<?>[] params = method.getParameterTypes();
    for (int i = 0; i < params.length; i++) {
        Class<?> param = params[i];
        sb.append(param.getName());
        if (i != params.length - 1) {
            sb.append(", ");
        }
    }
    sb.append(")");
    return sb.toString();
  }
  
  /**
    * Implements constructor by given constructor and puts it to {@link #code}
    *
    * @param constructor that to implement
    *
    * @throws ImplerException then no super call generated
    */
  private void implementConstructor(Constructor<?> constructor) throws ImplerException {
    code.append(modifiers(constructor.getModifiers()).replace("transient", " ")).append(" ");
    code.append(className).append("(");
    
    Class<?>[] params = constructor.getParameterTypes();
    for (int i = 0; i < params.length; i++) {
        code.append(getValidType(params[i]) ).append(" param").append(i);
        if (i != params.length - 1) code.append(", ");
    }
    code.append(") ");
    generateThrows(constructor.getExceptionTypes());
    code.append(" {").append(LS);
    generateSuperCall(constructor); 
    code.append(LS).append("}").append(LS);
  }
  
  /**
    * Generates throws section for given exceptions and puts it to {@link #code}
    *
    * @param exceptions array of {@link java.lang.Class} exceptions
    */
  private void generateThrows(Class<?>[] exceptions) {
    if (exceptions.length > 0) {
      code.append(" throws ");
      for (int i = 0; i < exceptions.length; i++) {
        code.append(exceptions[i].getName());
        if (i != exceptions.length - 1) code.append(", ");
      }
    }
  }
  
  /**
    * Generates call of super for given constructor and puts it to {@link #code}
    *
    * @param constructor {@link java.lang.reflect.Constructor} for that we need to generate super call
    * 
    * @throws ImplerException then no super constructor avaliable
    */
  private void generateSuperCall(Constructor<?> constructor) throws ImplerException {
    if (foundSuper) {
      code.append("super(");
      
      Class<?>[] params = constructor.getParameterTypes();
      for (int i = 0; i < params.length; i++) {
          code.append("(").append(getValidType(params[i])).append(") ").append(getDefaultValue(params[i]));
          if (i != params.length - 1) code.append(", ");
          
      }
      
      code.append(");");
    } else throw new ImplerException("No super constructor avalaible!");
  }
  
  /**
   * Implements method by given {@link java.lang.reflect.Method} instance. Stores it into {@link #code}. Implemented method will do nothing 
   * but return default value of their return type
   *
   * @param method {@link java.lang.reflect.Method} that we need to implement
   */
  private void implementMethod(Method method) {
    code.append("@Override").append(LS);

    code.append(modifiers(method.getModifiers()).replace("abstract", "").replace("transient", "")).append(" ");
    code.append(getValidType(method.getReturnType())).append(" ");
    code.append(method.getName()).append("(");
    Class<?>[] params = method.getParameterTypes();
    //
    for (int i = 0; i < params.length; i++) {
      Class<?> param = params[i];
      code.append(getValidType(param)).append(" ").append("p").append(i);
      if (i != params.length - 1) {
        code.append(", ");
      }
    }
    code.append(")");
    generateThrows(method.getExceptionTypes());
    code.append("{").append(LS);
    generateReturn(method.getReturnType());
    code.append("}").append(LS);
  }
   
  /**
    * Get valid type of given type-token
    *
    * @param param {@link java.lang.Class} of that we have to return valid type
    *
    * @return {@link java.lang.String} with type
    */
  private String getValidType(Class<?> param) {
    return getValidPackageString(param.getPackage()) + param.getSimpleName();
  }

  /**
    * Get valid package name of given package
    *
    * @param p package 
    *
    * @return {@link java.lang.String} with name
    */  
  private String getValidPackageString(Package p) {
    return (p == null ? "" : p.getName() + ".");
  }

  /**
    * Returns string representation of default value for given type
    *
    * @param param {@link java.lang.Class} from what we need to get default value
    *
    * @return {@link java.lang.String} with type
    */  
  private String getDefaultValue(Class<?> param) {
    switch (param.getSimpleName()) {
        case "byte":
        case "short":
        case "int":
            return " 0";
        case "long":
            return " 0L";
        case "float":
            return " 0.0f";
        case "double":
            return " 0.0d";
        case "char":
            return " '\u0000'";
        case "boolean":
            return " false";
        case "void":
            return " ";
        default:
            return " null";
    }
  }
  
  /**
    * Generates return section with default value of given type and puts it to {@link #code}
    *
    * @param ret {@link java.lang.Class} for that we have to generate return
    */
  private void generateReturn(Class<?> ret) {
    String s = getDefaultValue(ret);
    if (!s.equals(" ")) {
      code.append("return").append(s).append(";");
    }
  }
}