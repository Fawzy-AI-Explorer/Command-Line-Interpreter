import java.util.Arrays;

class Parser {
    protected String commandName ;
    protected String [] args;
    public boolean parse (String input) {
        /**
         * ls
         * mkdir hcc  sdd
         * Split the input into command name and arguments
         * "commandName arg1 arg2 arg3"
         * tokens[0] = "commandName"
         * tokens[1] = "arg1"
         * tokens[2] = "arg2"
         * tokens[3] = "arg3"
         **/
        String[] commandInput = input.split("\\s+");
        if (commandInput.length < 1) {
            return false;
        }
        this.commandName = commandInput[0];  // Set command name
        /**
         * "commandName arg1 arg2 arg3"
         * commandInput[0] = "commandName"
         * commandInput[1] = "arg1"
         * commandInput[2] = "arg2"
         * commandInput[3] = "arg3"
         * ---------------->
         * args[0] = "arg1"
         * args[1] = "arg2"
         * args[2] = "arg3"
         * **/
        if (commandInput.length > 1) {
            this.args = Arrays.copyOfRange(commandInput, 1, commandInput.length);
        } else {
            this.args = new String[0];
        }
        return true;
    }
    public String getCommandName () {
        return commandName;
    }
    public String [] getArgs (){return args; }
}
