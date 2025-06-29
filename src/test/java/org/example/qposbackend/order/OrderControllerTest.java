package org.example.qposbackend.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.example.qposbackend.data.DummyData.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {
  @Mock private OrderService orderService;
  @InjectMocks private OrderController orderController;
  
  private MockMvc mockMvc;
  private ObjectMapper objectMapper;
  private SaleOrder mockSaleOrder;

  @BeforeEach
  public void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
    objectMapper = new ObjectMapper();
    mockSaleOrder = getDummySaleOrder();
  }

  @Test
  public void testProcessOrder_WithValidOrder_ShouldReturnSuccess() throws Exception {
    // Arrange
    doNothing().when(orderService).processOrder(any(SaleOrder.class));
    String orderJson = objectMapper.writeValueAsString(mockSaleOrder);

    // Act & Assert
    mockMvc.perform(post("/order")
            .contentType(MediaType.APPLICATION_JSON)
            .content(orderJson))
            .andExpect(status().isCreated());

    verify(orderService).processOrder(any(SaleOrder.class));
  }

  @Test
  public void testProcessOrder_WithInvalidOrder_ShouldReturnNotAcceptable() throws Exception {
    // Arrange
    doThrow(new RuntimeException("Invalid order"))
        .when(orderService).processOrder(any(SaleOrder.class));
    String orderJson = objectMapper.writeValueAsString(mockSaleOrder);

    // Act & Assert
    mockMvc.perform(post("/order")
            .contentType(MediaType.APPLICATION_JSON)
            .content(orderJson))
            .andExpect(status().isNotAcceptable()); // Based on controller implementation

    verify(orderService).processOrder(any(SaleOrder.class));
  }

  @Test
  public void testProcessOrder_WithMalformedJson_ShouldReturnBadRequest() throws Exception {
    // Act & Assert
    mockMvc.perform(post("/order")
            .contentType(MediaType.APPLICATION_JSON)
            .content("invalid json"))
            .andExpect(status().isBadRequest());

    verify(orderService, never()).processOrder(any(SaleOrder.class));
  }
} 