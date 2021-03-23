package il.ac.bgu.se.bp.rest.controller;

import il.ac.bgu.se.bp.rest.request.DebugRequest;
import il.ac.bgu.se.bp.rest.request.RunRequest;
import il.ac.bgu.se.bp.rest.request.SetBreakpointRequest;
import il.ac.bgu.se.bp.rest.request.ToggleBreakpointsRequest;
import il.ac.bgu.se.bp.rest.response.BooleanResponse;

import java.security.Principal;

public interface BPjsIDERestController {

    void subscribeUser(String sessionId, Principal principal);

    BooleanResponse run(String userId, RunRequest code);
    BooleanResponse debug(String userId, DebugRequest code);

    BooleanResponse setBreakpoint(String userId, SetBreakpointRequest setBreakpointRequest);
    BooleanResponse toggleMuteBreakpoints(String userId, ToggleBreakpointsRequest toggleBreakpointsRequest);

    BooleanResponse stop(String userId);
    BooleanResponse stepOut(String userId);
    BooleanResponse stepInto(String userId);
    BooleanResponse stepOver(String userId);
    BooleanResponse continueRun(String userId);

    BooleanResponse nextSync(String userId);

}