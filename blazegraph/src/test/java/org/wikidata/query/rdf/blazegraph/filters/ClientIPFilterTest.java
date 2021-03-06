package org.wikidata.query.rdf.blazegraph.filters;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ClientIPFilterTest {

    private Filter filter = new ClientIPFilter();
    @Mock private HttpServletResponse response;
    @Mock private FilterChain chain;
    @Captor private ArgumentCaptor<ServletRequest> filteredRequest;

    @Test
    public void remoteAddrIsReturnedWhenNoRealIpHeaderIsPresent() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("1.2.3.4");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(filteredRequest.capture(), any(ServletResponse.class));
        assertThat(filteredRequest.getValue().getRemoteAddr(), equalTo("1.2.3.4"));
    }

    @Test
    public void realIpIsReturnedWhenHeaderIsPresent() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("1.2.3.4");
        request.addHeader("X-Client-IP", "4.3.2.1");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(filteredRequest.capture(), any(ServletResponse.class));
        assertThat(filteredRequest.getValue().getRemoteAddr(), equalTo("4.3.2.1"));
    }

    @Test
    public void caseOfHeaderIsIgnored() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("1.2.3.4");
        request.addHeader("x-client-ip", "4.3.2.1");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(filteredRequest.capture(), any(ServletResponse.class));
        assertThat(filteredRequest.getValue().getRemoteAddr(), equalTo("4.3.2.1"));
    }

    @Test
    public void dontWrapNonHttpRequests() throws IOException, ServletException {
        ServletRequest request = mock(ServletRequest.class);
        ServletResponse response = mock(ServletResponse.class);
        ArgumentCaptor<ServletRequest> filteredRequest = ArgumentCaptor.forClass(ServletRequest.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(filteredRequest.capture(), any(ServletResponse.class));

        assertThat(filteredRequest.getValue(), sameInstance(request));
    }

}
