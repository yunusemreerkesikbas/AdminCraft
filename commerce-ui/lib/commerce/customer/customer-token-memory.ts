let customerAccessToken: string | null = null;

export const getCustomerAccessToken = (): string | null => customerAccessToken;

export const setCustomerAccessToken = (accessToken: string): void => {
  customerAccessToken = accessToken;
};

export const clearCustomerAccessToken = (): void => {
  customerAccessToken = null;
};
