import axios, {AxiosResponse} from 'axios';

describe('Invoice API Endpoints', () => {
    const baseURL = 'http://localhost:3000';
    const apiClient = axios.create({
        baseURL,
        timeout: 5000,
        validateStatus: () => true
    });
    let createdInvoiceId: number | string | undefined;
    let authToken: string | undefined;

    beforeAll(async () => {
        // Wait for server to be ready
        let serverReady = false;
        let attempts = 0;
        const maxAttempts = 10;
        while (!serverReady && attempts < maxAttempts) {
            try {
                const response = await apiClient.get('/check');
                if (response.status === 200) serverReady = true;
            } catch {
                attempts++;
                if (attempts < maxAttempts) await new Promise(r => setTimeout(r, 1000));
            }
        }
        if (!serverReady) throw new Error('Server is not ready. Please start the server on port 3000 before running tests.');

        // Always register a fresh unique user to avoid login conflicts across runs
        const unique = `${Date.now()}_${Math.random().toString(36).slice(2, 8)}`;
        const registerPayload = {
            email: `invoice.test+${unique}@example.com`,
            password: 'testpassword',
            confirm_password: 'testpassword',
            full_name: 'Invoice Test User',
            date_of_birth: new Date().toISOString()
        };
        const registerResponse = await apiClient.post('/auth/register/email', registerPayload);
        if (![200, 201].includes(registerResponse.status) || !registerResponse.data?.access_token) {
            console.error('Register failed:', registerResponse.status, registerResponse.data);
            throw new Error('Failed to register test user for invoice tests.');
        }
        authToken = registerResponse.data.access_token;
        apiClient.defaults.headers.common['Authorization'] = `Bearer ${authToken}`;
    }, 25000);

    afterAll(async () => {
        // Cleanup: xóa invoice nếu còn tồn tại
        if (createdInvoiceId) {
            await apiClient.delete(`/invoices/delete-invoice/${createdInvoiceId}`);
        }
    });

    describe('POST /invoices/create-invoice', () => {
        it('should create a new invoice', async () => {
            const now = new Date();
            const dd = String(now.getDate()).padStart(2, '0');
            const mm = String(now.getMonth() + 1).padStart(2, '0');
            const yyyy = now.getFullYear();
            const payload = {
                doc: {
                    currency: 'USD',
                    payment_method: 'cash',
                    notes: 'Test invoice',
                },
                vendor: {
                    name: 'Test Vendor',
                    address: '123 Test St',
                    geo_hint: 'Test City',
                },
                datetime: {
                    date: `${dd}/${mm}/${yyyy}`,
                    time: '12:00',
                },
                items: [
                    {
                        raw_name: 'Item 1',
                        brand: 'Brand A',
                        category: 'Category X',
                        plant_based: false,
                        quantity: 2,
                        unit_price: 10,
                        line_total: 20,
                        matched_shopping_list: false,
                    },
                    {
                        raw_name: 'Item 2',
                        brand: 'Brand B',
                        category: 'Category Y',
                        plant_based: true,
                        quantity: 1,
                        unit_price: 20,
                        line_total: 20,
                        matched_shopping_list: false,
                    }
                ],
                totals: {
                    subtotal: 40,
                    discount: 0,
                    tax: 0,
                    grand_total: 40
                }
            };
            const response: AxiosResponse = await apiClient.post('/invoices/create-invoice', payload);
            if (response.status !== 201) console.error('Create invoice response:', response.data);
            expect(response.status).toBe(201);
            expect(response.data).toHaveProperty('message');
            expect(response.data).toHaveProperty('data');
            expect(response.data.data).toHaveProperty('id');
            createdInvoiceId = response.data.data.id;
        });

        it('should fail with invalid payload', async () => {
            const payload = {invalid: 'data'};
            const response: AxiosResponse = await apiClient.post('/invoices/create-invoice', payload);
            if (![400, 422].includes(response.status)) console.error('Invalid payload response:', response.data);
            expect([400, 422]).toContain(response.status);
        });
    });

    describe('GET /invoices/get-invoices', () => {
        it('should return a list of invoices', async () => {
            const response: AxiosResponse = await apiClient.get('/invoices/get-invoices');
            if (response.status !== 200) console.error('Get invoices response:', response.data);
            expect(response.status).toBe(200);
            expect(response.data).toHaveProperty('data');
            expect(Array.isArray(response.data.data)).toBe(true);
        });
    });

    describe('GET /invoices/get-invoices-by-id/:id', () => {
        it('should return the created invoice by id', async () => {
            if (!createdInvoiceId) return;
            const response: AxiosResponse = await apiClient.get(`/invoices/get-invoices-by-id/${createdInvoiceId}`);
            if (response.status !== 200) console.error('Get invoice by id response:', response.data);
            expect(response.status).toBe(200);
            expect(response.data).toHaveProperty('data');
            expect(response.data.data).toHaveProperty('id', createdInvoiceId);
        });

        it('should return 404 for non-existent invoice', async () => {
            const nonExistentId = '00000000-0000-0000-0000-000000000000';
            const response: AxiosResponse = await apiClient.get(`/invoices/get-invoices-by-id/${nonExistentId}`);
            if (![404].includes(response.status)) console.error('Get non-existent invoice response:', response.data);
            expect([404]).toContain(response.status);
        });
    });

    describe('PUT /invoices/update-invoice/:id', () => {
        it('should update the created invoice', async () => {
            if (!createdInvoiceId) return;
            const now = new Date();
            const dd = String(now.getDate()).padStart(2, '0');
            const mm = String(now.getMonth() + 1).padStart(2, '0');
            const yyyy = now.getFullYear();
            const payload = {
                doc: {
                    currency: 'USD',
                    payment_method: 'card',
                    notes: 'Updated invoice',
                },
                vendor: {
                    name: 'Updated Vendor',
                    address: '456 Updated St',
                    geo_hint: 'Updated City',
                },
                datetime: {
                    date: `${dd}/${mm}/${yyyy}`,
                    time: '14:00',
                },
                items: [
                    {
                        raw_name: 'Updated Item',
                        brand: 'Brand C',
                        category: 'Category Z',
                        plant_based: false,
                        quantity: 3,
                        unit_price: 15,
                        line_total: 45,
                        matched_shopping_list: false,
                    }
                ],
                totals: {
                    subtotal: 45,
                    discount: 0,
                    tax: 0,
                    grand_total: 45
                }
            };
            const response: AxiosResponse = await apiClient.put(`/invoices/update-invoice/${createdInvoiceId}`, payload);
            if (![200, 204].includes(response.status)) console.error('Update invoice response:', response.data);
            expect([200, 204]).toContain(response.status);
        });

        it('should fail to update with invalid data', async () => {
            if (!createdInvoiceId) return;
            const payload = {invalid: 'data'};
            const response: AxiosResponse = await apiClient.put(`/invoices/update-invoice/${createdInvoiceId}`, payload);
            if (![400, 422].includes(response.status)) console.error('Update invalid invoice response:', response.data);
            expect([400, 422]).toContain(response.status);
        });
    });

    describe('DELETE /invoices/delete-invoice/:id', () => {
        it('should delete the created invoice', async () => {
            if (!createdInvoiceId) return;
            const response: AxiosResponse = await apiClient.delete(`/invoices/delete-invoice/${createdInvoiceId}`);
            if (![200, 204].includes(response.status)) console.error('Delete invoice response:', response.data);
            expect([200, 204]).toContain(response.status);
        });

        it('should return 404 when deleting non-existent invoice', async () => {
            const nonExistentId = '00000000-0000-0000-0000-000000000000';
            const response: AxiosResponse = await apiClient.delete(`/invoices/delete-invoice/${nonExistentId}`);
            if (![404].includes(response.status)) console.error('Delete non-existent invoice response:', response.data);
            expect([404]).toContain(response.status);
        });
    });

    describe('Invoice API Edge Cases', () => {
        it('should handle invalid id format', async () => {
            const response: AxiosResponse = await apiClient.get('/invoices/get-invoices-by-id/invalid_id');
            if (![400, 404, 500].includes(response.status)) console.error('Invalid id format response:', response.data);
            expect([400, 404, 500]).toContain(response.status);
        });
    });
});
