/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core;

import java.io.IOException;

import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.mi.core.cdi.CSession;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIGDBSet;
import org.eclipse.cdt.debug.mi.core.command.MITargetAttach;
import org.eclipse.cdt.debug.mi.core.command.MITargetSelect;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Preferences;

/**
 * GDB/MI Plugin.
 */
public class MIPlugin extends Plugin {

	/**
	 * The plug-in identifier of the Java core support
	 * (value <code>"org.eclipse.jdt.core"</code>).
	 */
	public static final String PLUGIN_ID = "org.eclipse.cdt.debug.mi.core" ; //$NON-NLS-1$

	//The shared instance.
	private static MIPlugin plugin;

	/**
	 * The constructor
	 * @see org.eclipse.core.runtime.Plugin#Plugin(IPluginDescriptor)
	 */
	public MIPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
	}

	/**
	 * Returns the singleton.
	 */
	public static MIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Method createMISession.
	 * @param Process
	 * @param PTY
	 * @param int
	 * @param int
	 * @throws MIException
	 * @return MISession
	 */
	public MISession createMISession(Process process, PTY pty, int timeout, int type) throws MIException {
		return new MISession(process, pty, timeout, type);
	}

	/**
	 * Method createMISession.
	 * @param Process
	 * @param PTY
	 * @param type
	 * @throws MIException
	 * @return MISession
	 */
	public MISession createMISession(Process process, PTY pty, int type) throws MIException {
		MIPlugin plugin = getDefault();
		Preferences prefs = plugin.getPluginPreferences();
		int timeout = prefs.getInt(IMIConstants.PREF_REQUEST_TIMEOUT);
		return createMISession(process, pty, timeout, type);
	}

	/**
	 * Method createCSession.
	 * @param program
	 * @return ICDISession
	 * @throws MIException
	 */
	public ICDISession createCSession(String gdb, String program) throws IOException, MIException {
		PTY pty = null;
		try {
			pty = new PTY();
		} catch (IOException e) {
		}
		return createCSession(gdb, program, pty);
	}

	/**
	 * Method createCSession.
	 * @param program
	 * @return ICDISession
	 * @throws IOException
	 */
	public ICDISession createCSession(String gdb, String program, PTY pty) throws IOException, MIException {
		if (gdb == null || gdb.length() == 0) {
			gdb =  "gdb";
		}

		String[] args;
		if (pty != null) {
			args = new String[] {gdb, "-q", "-nw", "-tty", pty.getSlaveName(), "-i", "mi1", program};
		} else {
			args = new String[] {gdb, "-q", "-nw", "-i", "mi1", program};
		}

		Process pgdb = ProcessFactory.getFactory().exec(args);
		MISession session = createMISession(pgdb, pty, MISession.PROGRAM);
		// For windows we need to start the inferior in a new console window
		// to separate the Inferior std{in,out,err} from gdb std{in,out,err}
		try {
			CommandFactory factory = session.getCommandFactory();
			MIGDBSet set = factory.createMIGDBSet(new String[]{"new-console"});
			session.postCommand(set);
			MIInfo info = set.getMIInfo();
			if (info == null) {
				throw new MIException("No answer");
			}
		} catch (MIException e) {
			// We ignore this exception, for example
			// on GNU/Linux the new-console is an error.
		}
		return new CSession(session, false);
	}

	/**
	 * Method createCSession.
	 * @param program
	 * @param core
	 * @return ICDISession
	 * @throws IOException
	 */
	public ICDISession createCSession(String gdb, String program, String core) throws IOException, MIException {
		if (gdb == null || gdb.length() == 0) {
			gdb =  "gdb";
		}
		String[] args = new String[] {gdb, "--quiet", "-nw", "-i", "mi1", program, core};
		Process pgdb = ProcessFactory.getFactory().exec(args);
		MISession session = createMISession(pgdb, null, MISession.CORE);
		return new CSession(session);
	}

	/**
	 * Method createCSession.
	 * @param program
	 * @param pid
	 * @return ICDISession
	 * @throws IOException
	 */
	public ICDISession createCSession(String gdb, String program, int pid, String[] targetParams) throws IOException, MIException {
		if (gdb == null || gdb.length() == 0) {
			gdb =  "gdb";
		}
		String[] args = new String[] {gdb, "--quiet", "-nw", "-i", "mi1", program};
		Process pgdb = ProcessFactory.getFactory().exec(args);
		MISession session = createMISession(pgdb, null, MISession.ATTACH);
		MIInfo info = null;
		CommandFactory factory = session.getCommandFactory();
		if (targetParams != null && targetParams.length > 0) {
			MITargetSelect target = factory.createMITargetSelect(targetParams);
			session.postCommand(target);
			info = target.getMIInfo();
			if (info == null) {
				throw new MIException("No answer");
			}
		}
		MITargetAttach attach = factory.createMITargetAttach(pid);
		session.postCommand(attach);
		info = attach.getMIInfo();
		if (info == null) {
			throw new MIException("No answer");
		}
		//@@@ We have to manually set the suspended state when we attach
		session.getMIInferior().setSuspended();
		return new CSession(session, true);
	}

	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 */
	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return PLUGIN_ID;
		}
		return getDefault().getDescriptor().getUniqueIdentifier();
	}

	public static void debugLog(String message) {
		if (getDefault().isDebugging()) {
			// This is to verbose for a log file, better use the console.
			//	getDefault().getLog().log(StatusUtil.newStatus(Status.ERROR, message, null));
			// ALERT:FIXME: For example for big buffers say 4k length,
			// the console will simply blow taking down eclipse.
			// This seems only to happen in Eclipse-gtk and Eclipse-motif
			// on GNU/Linux, so it will be break in smaller chunks.
			while (message.length() > 100) {
				String partial = message.substring(0, 100);
				message = message.substring(100);
				System.err.println(partial + "\\");
			}
			if (message.endsWith("\n")) {
				System.err.print(message);
			} else {
				System.err.println(message);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#startup()
	 */
	public void startup() throws CoreException {
		super.startup();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#initializeDefaultPluginPrefrences()
	 */
	protected void initializeDefaultPluginPreferences() {
		getPluginPreferences().setDefault(IMIConstants.PREF_REQUEST_TIMEOUT, MISession.REQUEST_TIMEOUT);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#shutdown()
	 */
	public void shutdown() throws CoreException {
		savePluginPreferences();
		super.shutdown();
	}

}
