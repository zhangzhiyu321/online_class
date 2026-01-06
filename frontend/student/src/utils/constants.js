/**
 * 状态常量
 */
export const STATUS = {
  // 预约状态
  APPOINTMENT: {
    PENDING: 1,
    CONFIRMED: 2,
    COMPLETED: 3,
    CANCELLED: 4,
    REJECTED: 5
  },
  // 支付状态
  PAYMENT: {
    PENDING: 1,
    CONFIRMED: 2,
    COMPLETED: 3,
    REJECTED: 4
  },
  // 退款状态
  REFUND: {
    PENDING: 1,
    APPROVED: 2,
    REJECTED: 3,
    COMPLETED: 4
  },
  // 消息类型
  MESSAGE_TYPE: {
    TEXT: 1,
    FILE: 2,
    VOICE: 3,
    IMAGE: 4
  }
}

/**
 * 状态文本映射
 */
export const STATUS_TEXT = {
  appointment: {
    1: '待确认',
    2: '已确认',
    3: '已完成',
    4: '已取消',
    5: '已拒绝'
  },
  payment: {
    1: '待支付',
    2: '待确认',
    3: '已完成',
    4: '已拒绝'
  },
  refund: {
    1: '待审核',
    2: '已通过',
    3: '已拒绝',
    4: '已完成'
  }
}

/**
 * 状态类型映射（用于 Element Plus Tag）
 */
export const STATUS_TYPE = {
  appointment: {
    1: 'warning',
    2: 'info',
    3: 'success',
    4: 'info',
    5: 'danger'
  },
  payment: {
    1: 'warning',
    2: 'info',
    3: 'success',
    4: 'danger'
  },
  refund: {
    1: 'warning',
    2: 'success',
    3: 'danger',
    4: 'success'
  }
}

