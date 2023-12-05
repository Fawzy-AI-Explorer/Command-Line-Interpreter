import java.io.IOException;
import java.nio.file.*; // (FileSystems and Path).
import java.util.*;
import java.util.Vector;




class Terminal {
    private Parser parser;
    private Path currentPath;

    public Terminal(Parser parser) {
        this.parser = parser;
        this.currentPath = FileSystems.getDefault().getPath("C:/Users/htc/Documents");//create a Path object representing the specified path string
    }

    public String pwd (){
        return currentPath.toString();
    }// return type is a string .toString()

    public void cd(String[] args){
        if (args.length == 0) {
            // Case 1: cd takes no arguments, change current path to home directory
            addToHistory("cd");
            currentPath = FileSystems.getDefault().getPath("C:/Users/htc/Documents");

            /*
//
//                File parentDir = currentDir.getParentFile();
//                if (parentDir != null) {
//                    System.setProperty("user.dir", parentDir.getAbsolutePath());
//                } else {
//                    System.out.println("You are already in the root directory.");
//                }*/
        } else if (args.length == 1) {
            addToHistory("cd", args[0]);
            if ("..".equals(args[0])) {
                // Case 2: cd takes 1 argument which is '..', change current directory to previous directory
                /**
                 *  currentPath             ----->= /user/home/documents.
                 *  currentPath.getParent() ----->= /user/home.
                 *  =*{ {after this operation} }*=
                 *  currentPath----->= /user/home
                 * **/
                if (currentPath.getParent() != null) {
                    System.out.println(currentPath.getParent());
                    currentPath = currentPath.getParent();
                } else {
                    System.out.println("You are already in the root directory.");
                }
            } else {
                // Case 3: cd takes 1 argument which is either the full path or the relative path
                // Change current path to the provided path
                Path newPath = FileSystems.getDefault().getPath(args[0]);
                if (newPath.isAbsolute()) {
                    /**
                     * newPath.isAbsolute() checks if the provided path is an absolute path.
                     * it starts from the root directory (/user/home/documents)
                     * currentPath ----->= absolute path (newPath).*/
                    currentPath = newPath;
                } else {
                    /** current path--->= /user/home
                     * and the user provides "documents" as the argument,
                     * the resolved currentPath will be /user/home/documents.
                     * The resolve() joins the current path with the provided relative path.**/
                    currentPath = currentPath.resolve(newPath);
                }
            }
        } else {
            // Handle invalid input if there are more than 1 arguments
            System.out.println("Invalid input for cd command.");
        }
    }

    public void echo(String[] args) {
        if (args.length == 1) {
            addToHistory("echo", String.join(" ", args));
            System.out.println(String.join(" ", args));
        } else {
            System.out.println("Invalid input for echo command.");
        }
    }

    public void mkdir(String[] args) {
        addToHistory("mkdir", args[0]);
        /**
         * currentPath--->= /user/home
         * args = {"newDir", "/user/documents", "existingDir/newSubdirectory"};
         ========*First Iteration (arg: "newDir"):
         * newDirPath-->/user/home/newDir.
         * Directory -->/user/home/newDir is created.
         * Output: Directory created: /user/home/newDir.
         ========*Second Iteration (arg: "/user/documents"):
         * newDirPath---->/user/documents.
         * Directory----> /user/home/documents is created.
         * Output: Directory created: /user/documents.
         ==========*Third Iteration (arg: "existingDir/newSubdirectory"):
         * newDirPath----->/user/home/existingDir/newSubdirectory.
         * Directory------>/user/home/existingDir/newSubdirectory is created.
         * Output: Directory created: /user/home/existingDir/newSubdirectory.
         * **/
        //iterates through each argument  in the args
        if (args.length >= 1) {
            for (String arg : args) {
                Path newDirPath = FileSystems.getDefault().getPath(arg);
                if (!newDirPath.isAbsolute()) {
                    // If the argument is not an absolute path, resolve it against the current path
                    newDirPath = currentPath.resolve(newDirPath);
                }

                try {
                    Files.createDirectories(newDirPath);
                    System.out.println("Directory created: " + newDirPath.toString());
                } catch (Exception e) {
                    System.out.println("Error creating directory: " + newDirPath.toString());
                }
            }
        }else System.out.println("Invalid input for mkdir command");
    }

    public void rmdir (String[] args) {
        if (args.length != 1) {
            System.out.println("Invalid input for rmdir command. Usage: rmdir <directory_path_or_*>");
            return;
        }
        addToHistory("rmdir", args[0]);
        Path dirPath = currentPath;
        try {
            if ("*".equals(args[0])) {
                // Case: rmdir *
                /**
                 * start from dirPath
                 * step = 1
                 * .filter(Files::isDirectory) -->  filters the directories
                 * **/
                //, Files.walk is used to walk through the directory tree, sorting the paths in reverse order so that inner directories are deleted first.
                Files.walk(currentPath, 1)
                        .filter(Files::isDirectory) //Filters the entries to include only directories, excluding regular files.
                        .forEach(directory -> { // Iterates over the filtered directories.
                            try {
                                if (isEmptyDirectory(directory)) {
                                    Files.delete(directory);
                                    System.out.println("Directory removed: " + directory.toString());
                                }
                            } catch (IOException e) {
                                System.out.println("Error removing directory: " + directory.toString());
                            }
                        });
            } else {
                dirPath = currentPath.resolve(args[0]);
                // Case: rmdir <directory_path>
                if (Files.isDirectory(dirPath) && isEmptyDirectory(dirPath)) {
                    Files.delete(dirPath);
                    System.out.println("Directory removed: " + dirPath.toString());
                } else {
                    System.out.println("Error: The specified directory does not exist or is not empty.");
                }
            }
        } catch (DirectoryNotEmptyException e) {
            System.out.println("Error: Directory is not empty: " + dirPath.toString());
        } catch (IOException e) {
            System.out.println("Error removing directory: " + dirPath.toString());
        }
    }
    private boolean isEmptyDirectory(Path dir) throws IOException {
        //DirectoryStream allows you to iterate over the entries in a directory.
        //!dirStream.iterator().hasNext(): The iterator().hasNext() checks if the DirectoryStream has any entries.
        // If it doesn't have any entries, it means the directory is empty,
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir)) {
            return !dirStream.iterator().hasNext();
        }
    }

    /**
     * try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir)):
     * creating a DirectoryStream using Files.newDirectoryStream(dir).
     * A DirectoryStream is an iterable collection of Path objects representing the entries in a directory.
     * return !dirStream.iterator().hasNext();
     * Inside the try block, the method checks if the dirStream has any elements (files or directories)
     * using the iterator().hasNext() method
     * .**/


    public void cp(String[] args) {
        if (args.length != 2) {
            System.out.println("Invalid input for cp command. Usage: cp <source_file> <destination_file>");
            return;
        }
        addToHistory("cp", args[0], args[1]);
        Path sourcePath = currentPath.resolve(args[0]);
        Path destinationPath = currentPath.resolve(args[1]);

        try {
            /**
             * StandardCopyOption.REPLACE_EXISTING:
             *  An option that specifies that if the target file already exists,it should be replaced.
             *  If this option is not provided and the target file already exists,
             *  "FileAlreadyExistsException" will be thrown.
             **/
            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File copied: " + sourcePath.toString() + " -> " + destinationPath.toString());
        }catch (NoSuchFileException e) {
            System.out.println("Error: Source file not found.");
        } catch (FileAlreadyExistsException e) {
            System.out.println("Error: Destination file already exists.");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }

    }

    public void cpReverse(String[] args) {
        if (args.length != 2) {
            System.out.println("Invalid input for cp -r command. Usage: cp -r <source_file> <destination_file>");
            return;
        }
        addToHistory("cp -r", args[0], args[1]);
        Path sourcePath = currentPath.resolve(args[0]);
        Path destinationPath = currentPath.resolve(args[1]);
        try {
            /**
             * StandardCopyOption.REPLACE_EXISTING:
             *  An option that specifies that if the target file already exists,it should be replaced.
             *  If this option is not provided and the target file already exists,
             *  "FileAlreadyExistsException" will be thrown.
             **/
            Files.copy(destinationPath, sourcePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File copied: " + destinationPath.toString() + " -> " + sourcePath.toString());
        }catch (NoSuchFileException e) {
            System.out.println("Error: Source file not found.");
        } catch (FileAlreadyExistsException e) {
            System.out.println("Error: Destination file already exists.");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }

    }

    public void rm(String[] args) {
        if (args.length != 1) {
            System.out.println("Invalid input for rm command. Usage: rm <file_name>");
            return;
        }
        addToHistory("rm", args[0]);
        Path filePath = currentPath.resolve(args[0]);
        /***
         * Files.isRegularFile(path)--> true if the specified path points to a regular file in the file system.
         * regular file --> is a file that contains data and is not a directory.
         * */
        try {
            if (Files.exists(filePath) && Files.isRegularFile(filePath)) {  //regular --> not a device file
                Files.delete(filePath);
                System.out.println("File removed: " + filePath.toString());
            } else {
                System.out.println("Error: The specified file does not exist or is not a regular file.");
            }
        } catch (IOException e) {
            System.out.println("Error removing file: " + e.getMessage());
        }
    }


    public void cat(String[] args) {
        if (args.length == 1) {
            addToHistory("cat", args[0]);
            // Case: cat <file_name> ---> print the file’s content
            Path filePath = currentPath.resolve(args[0]);
            try {
                //Files.exists(filePath) --> checks if a specified Path object exists in the file system
                if (Files.exists(filePath) && Files.isRegularFile(filePath)) {
                    String fileContent = new String(Files.readAllBytes(filePath));
                    System.out.println(fileContent);
                } else {
                    System.out.println("Error: The specified file does not exist or is not a regular file.");
                }
            } catch (IOException e) {
                System.out.println("Error reading file: " + e.getMessage());
            }
        } else if (args.length == 2) {
            // Case: cat <file1> <file2> --->concatenates the content of the 2 files and prints it.
            Path filePath1 = currentPath.resolve(args[0]);
            Path filePath2 = currentPath.resolve(args[1]);
            try {
                if (Files.exists(filePath1) && Files.isRegularFile(filePath1) &&
                        Files.exists(filePath2) && Files.isRegularFile(filePath2)) {
                    String content1 = new String(Files.readAllBytes(filePath1));
                    String content2 = new String(Files.readAllBytes(filePath2));
                    System.out.println(content1 + content2);
                } else {
                    System.out.println("Error: One or both of the specified files do not exist or are not regular files.");
                }
            } catch (IOException e) {
                System.out.println("Error reading files: " + e.getMessage());
            }
        } else {
            System.out.println("Invalid input for cat command. Usage: cat <file_name> OR cat <file1> <file2>");
        }
    }

    public void ls() {
        addToHistory("ls");
        //DirectoryStream is used to iterate over the contents of the directory.
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(currentPath)) {
            List<Path> files = new ArrayList<>();
            for (Path entry : stream) {
                files.add(entry);
            }
            //sorts the files based on their names.
            files.sort(Comparator.comparing(Path::getFileName));

            for (Path file : files) {
                System.out.println(file.getFileName());
            }
        } catch (Exception e) {
            System.out.println("Error listing directory contents: " + e.getMessage());
        }
    }

    public void lsReverse() {
        addToHistory("ls -r", parser.args[0]);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(currentPath)) {
            List<Path> files = new ArrayList<>();
            for (Path entry : stream) {
                files.add(entry);
            }

            files.sort(Comparator.comparing(Path::getFileName).reversed());

            for (Path file : files) {
                System.out.println(file.getFileName());
            }
        } catch (Exception e) {
            System.out.println("Error listing directory contents: " + e.getMessage());
        }
    }

    public void touch(String[] args) {
        if (args.length != 1) {
            System.out.println("Invalid input for touch command. Usage: touch <file_path>");
            return;
        }
        addToHistory("touch", args[0]);

        Path filePath = FileSystems.getDefault().getPath(args[0]);
        if (!filePath.isAbsolute()) {
            // If the argument is a relative path, resolve it against the current path
            filePath = currentPath.resolve(filePath);
        }

        try {
            Files.createFile(filePath);
            System.out.println("File created: " + filePath.toString());
        } catch (FileAlreadyExistsException e) {
            System.out.println("Error: File already exists at " + filePath.toString());
        } catch (Exception e) {
            System.out.println("Error creating file: " + e.getMessage());
        }
    }

    Vector<String> history_V = new Vector<>();

    public void addToHistory(String comm,String arg){
        history_V.add(comm + " " + arg);
    }
    public void addToHistory(String comm,String arg, String arg2){
        history_V.add(comm + " " + arg + " " + arg2);
    }
    public void addToHistory(String comm){
        history_V.add(comm);
    }
    public void printHistory(){
        int commandno = 1;
        history_V.add("history");
        for (String element : history_V) {
            System.out.println(String.valueOf(commandno) + " " + element);
            commandno++;

        }
    }

    public void chooseCommandAction(){
        String commandName = parser.getCommandName();
        String[] args = parser.getArgs();

        if ("pwd".equals(commandName)) {
            addToHistory("pwd");
            System.out.println(pwd());
        } else if ("cd".equals(commandName)) {
            cd(args);
        } else if ("echo".equals(commandName)) {
            echo(args);
        } else if ("mkdir".equals(commandName)) {
            mkdir(args);
        }else if ("cp".equals(commandName)) {
            if (args.length >= 2 && "-r".equals(args[0])) {
                cpReverse(Arrays.copyOfRange(args, 1, args.length));
            } else if (args.length == 2) {
                cp(args);
            }
        }
        else if ("rmdir".equals(commandName)) {
            rmdir(args);
        }else if ("rm".equals(commandName)) {
            rm(args);
        }else if ("cat".equals(commandName)) {
            cat(args);
        }else if ("ls".equals(commandName)) {
            if (args.length == 1 && "-r".equals(args[0])) {
                lsReverse();
            } else {
                ls();
            }
        }
        else if ("touch".equals(commandName)) {
            touch(args);
        } else if ("history".equals(commandName)) {
            printHistory();
        } else {
            System.out.println("Command not recognized: " + commandName);
        }
    }
}



