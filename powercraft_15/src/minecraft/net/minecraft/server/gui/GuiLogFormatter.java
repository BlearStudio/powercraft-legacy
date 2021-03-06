package net.minecraft.server.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

@SideOnly(Side.SERVER)
class GuiLogFormatter extends Formatter
{
    /** Reference to the GuiLogOutputHandler. */
    final GuiLogOutputHandler outputHandler;

    GuiLogFormatter(GuiLogOutputHandler par1GuiLogOutputHandler)
    {
        this.outputHandler = par1GuiLogOutputHandler;
    }

    public String format(LogRecord par1LogRecord)
    {
        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append(" [").append(par1LogRecord.getLevel().getName()).append("] ");
        stringbuilder.append(this.formatMessage(par1LogRecord));
        stringbuilder.append('\n');
        Throwable throwable = par1LogRecord.getThrown();

        if (throwable != null)
        {
            StringWriter stringwriter = new StringWriter();
            throwable.printStackTrace(new PrintWriter(stringwriter));
            stringbuilder.append(stringwriter.toString());
        }

        return stringbuilder.toString();
    }
}
