package il.ac.bgu.se.bp.debugger.engine;

import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.se.bp.debugger.RunnerState;
import il.ac.bgu.se.bp.debugger.commands.StepInto;
import il.ac.bgu.se.bp.debugger.engine.events.BPStateEvent;
import il.ac.bgu.se.bp.execution.manage.ProgramValidatorImpl;
import il.ac.bgu.se.bp.socket.state.BPDebuggerState;
import il.ac.bgu.se.bp.utils.DebuggerStateHelper;
import il.ac.bgu.se.bp.utils.Pair;
import il.ac.bgu.se.bp.utils.asyncHelper.AsyncOperationRunnerImpl;
import il.ac.bgu.se.bp.utils.observer.BPEvent;
import il.ac.bgu.se.bp.utils.observer.Publisher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mozilla.javascript.tools.debugger.Dim;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DebuggerEngineImplTest {

    private final static String TEST_FILENAME = "BPJSTestEngine.js";
    private static final String debuggerId = "6981cb0a-f871-474b-98e9-faf7c02e18a4";

    private final static int[] BREAKPOINTS_LINES = new int[]{2, 4};
    private final static Map<Integer, Boolean> breakpoints = new HashMap<>();

    private final ExecutorService execSvc = ExecutorServiceMaker.makeWithName("TEST_");
    private final static BlockingQueue<BPDebuggerState> onStateChangedQueue = new ArrayBlockingQueue<>(5);
    private RunnerState state = new RunnerState();
    private BPDebuggerState expectedState;

    @Mock
    private DebuggerStateHelper debuggerStateHelper;

    @Mock
    private Publisher<BPEvent> publisher;

    @Spy
    private AsyncOperationRunnerImpl asyncOperationRunner;

    @Spy
    private ProgramValidatorImpl programValidator;

    private DebuggerEngineImpl debuggerEngine;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        debuggerEngine = new DebuggerEngineImpl(debuggerId, TEST_FILENAME, state, debuggerStateHelper, "debuggerTestId");
        setMockPublisher();
        onStateChangedQueue.clear();
        Arrays.stream(BREAKPOINTS_LINES).forEach(lineNumber -> breakpoints.put(lineNumber, true));
    }

    private void setMockPublisher() {
        try {
            FieldSetter.setField(debuggerEngine, DebuggerEngineImpl.class.getDeclaredField("publisher"), publisher);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testIsBreakPointAllowed() throws InterruptedException {
        doAnswer(invocation -> onStateChangedTester(invocation.getArgument(0, BPStateEvent.class))).when(publisher).notifySubscribers(any());
        expectedState = new BPDebuggerState(new LinkedList<>(), null);
        BProgram bProg = new ResourceBProgram(TEST_FILENAME);
        BProgramSyncSnapshot bProgramSyncSnapshot = bProg.setup();
        assertFalse(debuggerEngine.isBreakpointAllowed(50)); // after EOF
        assertFalse(debuggerEngine.isBreakpointAllowed(16)); //end of function }
        assertTrue(debuggerEngine.isBreakpointAllowed(3));
        assertTrue(debuggerEngine.isBreakpointAllowed(1));

        debuggerEngine.setSyncSnapshot(bProgramSyncSnapshot);
        bProgramSyncSnapshot.start(execSvc);
    }

//    @Test(expected = IllegalArgumentException.class)
//    public void testSetBreakpointIllegalArgument() {
//        expectedState = new BPDebuggerState(new LinkedList<>(), null);
//        BProgram bProg = new ResourceBProgram(TEST_FILENAME);
//        BProgramSyncSnapshot bProgramSyncSnapshot = bProg.setup();
//
//        debuggerEngine.setBreakpoint(50, true);
//    }

    @Test
    public void testSetBreakpointPositive() {
        doAnswer(invocation -> onStateChangedTester(invocation.getArgument(0, BPStateEvent.class))).when(publisher).notifySubscribers(any());
        expectedState = new BPDebuggerState(new LinkedList<>(), null);
        BProgram bProg = new ResourceBProgram(TEST_FILENAME);
        BProgramSyncSnapshot bProgramSyncSnapshot = bProg.setup();
        debuggerEngine.setSyncSnapshot(bProgramSyncSnapshot);
        debuggerEngine.setBreakpoint(2, true);
        when(debuggerStateHelper.generateDebuggerState(any(), any(), any(), any())).thenAnswer(invocationOnMock -> {
            int line = invocationOnMock.getArgument(2, Dim.ContextData.class).getFrame(0).getLineNumber();
            assertEquals(2, line);
            debuggerEngine.continueRun();
            return new BPDebuggerState();
        });
        try {
            bProgramSyncSnapshot = bProgramSyncSnapshot.start(execSvc);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        verify(debuggerStateHelper, times(1)).generateDebuggerState(any(), any(), any(),any());
    }

    @Test
    public void testEnvChangedInBreakPoints() throws InterruptedException {
        doAnswer(invocation -> onStateChangedTester(invocation.getArgument(0, BPStateEvent.class))).when(publisher).notifySubscribers(any());
        expectedState = new BPDebuggerState(new LinkedList<>(), null);
        BProgram bProg = new ResourceBProgram(TEST_FILENAME);
        BProgramSyncSnapshot bProgramSyncSnapshot = bProg.setup();
        debuggerEngine.setSyncSnapshot(bProgramSyncSnapshot);

        Arrays.stream(BREAKPOINTS_LINES).forEach(lineNumber -> debuggerEngine.setBreakpoint(lineNumber, true));

        doCallRealMethod().when(debuggerStateHelper).setRecentlyRegisteredBThreads(any());
        doCallRealMethod().when(debuggerStateHelper).getLastState();
        doCallRealMethod().when(debuggerStateHelper).peekNextState(any(),any(),any(),any());

        Set<BThreadSyncSnapshot> recentlyRegisteredBThreads = bProg.getRecentlyRegisteredBthreads();
        Set<Pair<String, Object>> recentlyRegistered = new HashSet<>();
        for (BThreadSyncSnapshot b : recentlyRegisteredBThreads) {
            recentlyRegistered.add(new Pair<>(b.getName(), b.getEntryPoint()));
        }
        debuggerStateHelper.setRecentlyRegisteredBThreads(recentlyRegistered);
        try {
            FieldSetter.setField(debuggerStateHelper, DebuggerStateHelper.class.getDeclaredField("newBTInterpreterFrames"), new HashMap<>());
            FieldSetter.setField(debuggerStateHelper, DebuggerStateHelper.class.getDeclaredField("syncSnapshotHolder"), new SyncSnapshotHolderImpl());
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        when(debuggerStateHelper.generateDebuggerState(any(), any(), any(),any())).thenAnswer(invocationOnMock -> {
            debuggerEngine.addCommand(new StepInto());
            return invocationOnMock.callRealMethod();
        });
        try {
            bProgramSyncSnapshot = bProgramSyncSnapshot.start(execSvc);
            doCallRealMethod().when(debuggerStateHelper).generateDebuggerState(any(), any(), any(),any());
            state.setDebuggerState(RunnerState.State.SYNC_STATE);
            debuggerEngine.setSyncSnapshot(bProgramSyncSnapshot);
            debuggerEngine.onStateChanged();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        BPDebuggerState state = onStateChangedQueue.take();
        expectedState = ExpectedResults.testEnvChangedInBreakPoints_ENV1();
        assertEquals(expectedState, state);
        state = onStateChangedQueue.take();
        expectedState = ExpectedResults.testEnvChangedInBreakPoints_ENV2();
        assertEquals(expectedState, state);
        debuggerEngine.onStateChanged();
        onStateChangedQueue.take();
        state = onStateChangedQueue.take();
        expectedState = ExpectedResults.testEnvChangedInBreakPoints_ENV3();
        assertEquals(expectedState, state);
    }


    private static Void onStateChangedTester(BPStateEvent bpStateEvent) {
        assertEquals(debuggerId, bpStateEvent.getDebuggerId());
        onStateChangedQueue.add(bpStateEvent.getEvent());
        return null;
    }

}